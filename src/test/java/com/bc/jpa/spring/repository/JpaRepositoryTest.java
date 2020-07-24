package com.bc.jpa.spring.repository;

import com.bc.jpa.spring.EntityIdAccessorImpl;
import com.bc.jpa.spring.MyTestConfiguration;
import com.bc.jpa.spring.TestConfig;
import com.bc.jpa.spring.domain.Blog;
import com.bc.jpa.spring.domain.enums.BlogType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.persistence.PersistenceContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hp
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MyTestConfiguration.class }, loader = AnnotationConfigContextLoader.class)
@PersistenceContext
@Transactional("hibernateTransactionManager")
public class JpaRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {
    
    @Test
    public void multipleCreatesThenFindThenUpdateThenDelete_shouldReturnSuccessfully() {
        info("multipleCreatesThenFindThenUpdateThenDelete_shouldReturnSuccessfully");
        final JpaRepository repo = this.getInstance(this.getEntityType());
        final int offset = (int)repo.count();
        final int limit = 1000;
        final List<Blog> blogList = this.getInitializedEntities(offset, limit);
        for(Blog blog : blogList) {
            repo.saveAndFlush(blog); 
            debug("Created blog with id: " + getIdOptional(blog).orElse(null));
        }
        
        final Function<Integer, Blog> find = (id) -> (Blog)repo.findById(id).orElse(null);
        this.test(blogList, "Find", find, offset, limit);
        
        final Function<Integer, Blog> update = (id) -> {
            try{
                final Blog blog = (Blog)repo.findById(id).orElse(null);
                blog.setDescription("Updated Description " + blog.getId());
                repo.saveAndFlush(blog);
                return blog;
            }catch(RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        };
        this.test(blogList, "Update", update, offset, limit);
        
        final Function<Integer, Blog> delete = (id) -> {
            try{
                final Blog blog = (Blog)repo.findById(id).orElse(null);
                repo.deleteById(id);
                return blog;
            }catch(RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        };    
        this.test(blogList, "Delete", delete, offset, limit);
    }
    
    @Test
    public void count_shouldReturnValidCount() {
        info("count_shouldReturnValidCount");
        final JpaRepository instance = this.getInstance(this.getEntityType());
        final long expResult = instance.count();
        final long result = instance.count();
        assertThat(result, is(expResult));
    }

    @Test
    public void count_givenEntityInserted_shouldReturnNonZeroCount() {
        info("count_givenEntityInserted_shouldReturnNonZeroCount");
        final JpaRepository instance = this.getInstance(this.getEntityType());
        final long count = instance.count();
//        instance.save(this.getInitializedEntity()); Failed
        instance.saveAndFlush(this.getInitializedEntity());
        final long expResult = count + 1;
        final long result = instance.count();
        assertThat(result, is(expResult));
    }

    @Test
    public void hasRecords_shouldReturnValidResult() {
        info("hasRecords_shouldReturnValidResult");
        final JpaRepository instance = this.getInstance(this.getEntityType());
        final long count = instance.count();
        final boolean expResult = count > 0;
        final boolean result = ! instance.findAll(PageRequest.of(0, 1)).isEmpty();
        assertThat(result, is(expResult));
    }

    @Test
    public void getIdOptional_givenEntityWithNoId_shouldReturnOptionalWithNullValue() {
        info("getIdOptional_givenEntityWithNoId_shouldReturnOptionalWithNullValue");
        final Object entity = this.createEntity();
        final JpaRepository instance = this.getInstance(this.getEntityType());
        final Optional expResult = Optional.ofNullable(null);
        final Optional result = getIdOptional(entity);
        assertThat(result, is(expResult));
    }
    
    @Test
    public void getIdOptional_givenEntityWithId_shouldReturnOptionalWithIdValue() {
        info("getIdOptional_givenEntityWithId_shouldReturnOptionalWithIdValue");
        final Blog entity = this.getInitializedEntity();
        final JpaRepository instance = this.getInstance(this.getEntityType());
        instance.saveAndFlush(entity);
        final Optional expResult = Optional.of(entity.getId());
        final Optional result = getIdOptional(entity);
        assertThat(result, is(expResult));
    }

    @Test
    public void create_givenValidEntity_shouldCreateTheEntitySuccessfully() {
        info("create_givenValidEntity_shouldCreateTheEntitySuccessfully");
        final Object entity = this.getInitializedEntity();
        final JpaRepository instance = this.getInstance(this.getEntityType());
//        instance.save(entity); Failed
        instance.saveAndFlush(entity);
        final Object id = getIdOptional(entity).orElse(null);
        assertThat(id, is(notNullValue()));
    }
    
    @Test
    public void create_givenNonValidEntity_shouldThrowException() {
        final String method = "create_givenNonValidEntity_shouldThrowException";
        info(method);
        try{
            final Object entity = new Object();
            final JpaRepository repo = this.getInstance(this.getEntityType());
            repo.save(entity);
            fail(method + " should throw exception, but completed exception");
        }catch(RuntimeException ignored) {}
    }
    
    public Optional<Object> getIdOptional(Object entity) {
        return new EntityIdAccessorImpl(this.getTestConfig()
                .getEntityManagerFactory()).getValueOptional(entity);
    }
    
    public String getTableName(Object entity) {
        return "blog";
    }

    public void test(
            List<Blog> blogList, String fnKey, 
            Function<Integer, Blog> fn, int offset, int limit) {
        for(int i=0; i<limit; i++) {
            final int id = offset + i + 1;
            debug(fnKey + " blog with id: " + id);
            final Blog blog = fn.apply(id);
            assertThat(blog, is(blogList.get(i)));
        }
    }
    
    public String getTableName() {
        return "blog";
    }
    
    public Blog getInitializedEntity() {
        return this.getInitializedEntities(1).get(0);
    }

    public List<Blog> getInitializedEntities(int limit) {
        final JpaRepository repo = this.getInstance(this.getEntityType());
        final int offset = (int)repo.count();
        return this.getInitializedEntities(offset, limit);
    }
    
    public List<Blog> getInitializedEntities(int offset, int limit) {
        final List<Blog> result = new ArrayList<>(limit);
        for(int i=0; i<limit; i++) {
            final int id = offset + i;
            final Blog blog = this.createEntity();
            blog.setDescription("Test blog description " + id);
            blog.setEnabled(Math.random() > 0.5);
            blog.setHandle("Test Blog Handle " + id);
            blog.setTimeCreated(new Date());
            blog.setType(this.getRandomBlogType());
            result.add(blog);
        }
        return result;
    }
    
    public BlogType getRandomBlogType() {
        final BlogType [] values = BlogType.values();
        final double d = Math.random() * values.length;
        final int n = (int)(d >= values.length ? values.length - 1 : d);
        return values[n];
    }

    public <E, ID> JpaRepository<E, ID> getInstance(){
        return this.getInstance(this.getEntityType());
    }
    
    public <E> JpaRepository<E, Object> getInstance(Class<E> entityType){
        return this.getTestConfig().getJpaRepo(entityType);
    }
    
    public Class getEntityType() {
        return Blog.class;
    }
    
    public Blog createEntity() {
        return new Blog();
    }

    public TestConfig getTestConfig() {
        return new TestConfig();
    }
    
    public void info(Object msg) {
        System.out.println(msg);
    }
    
    public void debug(Object msg) {
        if(TestConfig.DEBUG) {
            System.out.println(msg);
        }
    }
}
