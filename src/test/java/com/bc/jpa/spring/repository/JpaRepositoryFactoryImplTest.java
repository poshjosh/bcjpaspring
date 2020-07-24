package com.bc.jpa.spring.repository;

import com.bc.jpa.spring.MyTestConfiguration;
import com.bc.jpa.spring.TestConfig;
import com.bc.jpa.spring.domain.Blog;
import com.bc.jpa.spring.domain.Post;
import com.bc.jpa.spring.domain.enums.BlogType;
import java.util.Date;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author hp
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MyTestConfiguration.class })
public class JpaRepositoryFactoryImplTest {
    
    public JpaRepositoryFactoryImplTest() {}
    
    @BeforeEach
    public void setUp() {
        this.getInstance().forEntity(Post.class).deleteAll();
        this.getInstance().forEntity(Blog.class).deleteAll();
    }
    
    /**
     * Test of getEntityManagerFactory method, of class JpaRepositoryFactoryImpl.
     */
    @Test
    public void testGetEntityManagerFactory() {
        System.out.println("getEntityManagerFactory");
        
        final JpaRepositoryFactory instance = this.getInstance();
        
        final int blogCount = 10;
        final int postsPerBlog = 100;
        
        for(int i = 0; i<blogCount; i++) {
            
            final Blog blog = new Blog();
            blog.setEnabled(true);
            blog.setHandle("Sample Blog Handle - " + i);
            blog.setTimeCreated(new Date());
            blog.setType(BlogType.MOVIES);
            // We use a new Instance for each save here
            instance.forEntity(Blog.class).saveAndFlush(blog);
            
            final int totalBlogs = i + 1;
            
            // We use a new Instance for each find here
            assertThat(instance.forEntity(Blog.class).findAll().size(), is(totalBlogs));
            
            final JpaRepository<Post, Object> jpa = instance.forEntity(Post.class);
            
            for(int j =0; j<postsPerBlog; j++) {
                final Post post = new Post();
                post.setBlog(blog);
                post.setContent("This is the content of the post " + j + ", of blog: " + i);
                post.setTimeCreated(new Date());
                jpa.saveAndFlush(post);
            }
            
            final int totalPosts = totalBlogs * postsPerBlog;
            
            assertThat(jpa.findAll().size(), is(totalPosts));
        }
    }
    
    public JpaRepositoryFactory getInstance() {
        return new JpaRepositoryFactoryImpl(
                this.getTestConfig().getEntityManagerFactory(), (cls) -> true);
    }

    public TestConfig getTestConfig() {
        return new TestConfig();
    }
}
