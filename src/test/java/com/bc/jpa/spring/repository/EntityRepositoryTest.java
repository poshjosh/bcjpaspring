package com.bc.jpa.spring.repository;

import com.bc.jpa.spring.TestConfig;
import com.bc.jpa.spring.domain.Blog;
import com.bc.jpa.spring.domain.enums.BlogType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @author hp
 */
public class EntityRepositoryTest {
    
    @Test
    public void getTableName_shouldReturnValidTableName() {
        System.out.println("getTableName_shouldReturnValidTableName");
        final EntityRepository instance = this.getInstance(this.getEntityType());
        final String expResult = this.getTableName();
        final String result = instance.getTableName();
        assertThat(result, is(expResult));
    }

    @Test
    public void getTableName_givenNonValidDomainType_shouldThrowException() {
        final String method = "getTableName_givenNonValidDomainType_shouldThrowException";
        System.out.println(method);
        try{
            final EntityRepository instance = this.getInstance(Object.class);
            instance.getTableName();
            fail(method + " should fail, but completed execution");
        }catch(RuntimeException ignored) { }
    }
    
    @Test
    public void multipleCreatesThenFindThenUpdateThenDelete_shouldReturnSuccessfully() {
        info("multipleCreatesThenFindThenUpdateThenDelete_shouldReturnSuccessfully");
        final EntityRepository<Blog> repo = this.getInstance(Blog.class);
        final int offset = (int)repo.count();
        final int limit = 1000;
        final List<Blog> blogList = this.getInitializedEntities(offset, limit);
        for(Blog blog : blogList) {
            repo.create(blog); 
            debug("Created blog with id: " + repo.getIdOptional(blog).orElse(null));
        }
        
        final Function<Integer, Blog> find = (id) -> repo.find(id);
        this.test(blogList, "Find", find, offset, limit);
        
        final Function<Integer, Blog> update = (id) -> {
            try{
                final Blog blog = repo.find(id);
                blog.setDescription("Updated Description " + blog.getId());
                repo.update(blog); 
                return blog;
            }catch(RuntimeException e) {
                e.printStackTrace();
                throw e;
            }
        };
        this.test(blogList, "Update", update, offset, limit);
        
        final Function<Integer, Blog> delete = (id) -> {
            try{
                final Blog blog = repo.find(id);
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
        final EntityRepository instance = this.getInstance();
        final long expResult = instance.count();
        final long result = instance.count();
        assertThat(result, is(expResult));
    }

    @Test
    public void count_givenEntityInserted_shouldReturnNonZeroCount() {
        info("count_givenEntityInserted_shouldReturnNonZeroCount");
        final EntityRepository instance = this.getInstance();
        final long count = instance.count();
        instance.create(this.getInitializedEntity());
        final long expResult = count + 1;
        final long result = instance.count();
        assertThat(result, is(expResult));
    }

    @Test
    public void hasRecords_shouldReturnValidResult() {
        info("hasRecords_shouldReturnValidResult");
        final EntityRepository instance = this.getInstance();
        final long count = instance.count();
        final boolean expResult = count > 0;
        final boolean result = instance.hasRecords();
        assertThat(result, is(expResult));
    }

    @Test
    public void getIdOptional_givenEntityWithNoId_shouldReturnOptionalWithNullValue() {
        info("getIdOptional_givenEntityWithNoId_shouldReturnOptionalWithNullValue");
        final Object entity = this.createEntity();
        final EntityRepository instance = this.getInstance();
        final Optional expResult = Optional.ofNullable(null);
        final Optional result = instance.getIdOptional(entity);
        assertThat(result, is(expResult));
    }
    
    @Test
    public void getIdOptional_givenEntityWithId_shouldReturnOptionalWithIdValue() {
        info("getIdOptional_givenEntityWithId_shouldReturnOptionalWithIdValue");
        final Blog entity = this.getInitializedEntity();
        final EntityRepository instance = this.getInstance();
        instance.create(entity);
        final Optional expResult = Optional.of(entity.getId());
        final Optional result = instance.getIdOptional(entity);
        assertThat(result, is(expResult));
    }

    @Test
    public void create_givenValidEntity_shouldCreateTheEntitySuccessfully() {
        info("create_givenValidEntity_shouldCreateTheEntitySuccessfully");
        final Object entity = this.getInitializedEntity();
        final EntityRepository instance = this.getInstance();
        instance.create(entity);
        final Object id = instance.getIdOptional(entity).orElse(null);
        assertThat(id, is(notNullValue()));
    }
    
    @Test
    public void create_givenNonValidEntity_shouldThrowException() {
        final String method = "create_givenNonValidEntity_shouldThrowException";
        info(method);
        try{
            final Object entity = new Object();
            final EntityRepository instance = this.getInstance();
            instance.create(entity);
            fail(method + " should throw exception, but completed exception");
        }catch(RuntimeException ignored) {}
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
        final EntityRepository<Blog> repo = this.getInstance(Blog.class);
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

    public <E> EntityRepository<E> getInstance(){
        return this.getInstance(this.getEntityType());
    }
    
    public <E> EntityRepository<E> getInstance(Class<E> entityType){
        return this.getTestConfig().getEntityRepo(entityType);
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
