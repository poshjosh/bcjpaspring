package com.bc.jpa.spring;

import com.bc.jpa.spring.domain.Blog;
import com.bc.jpa.spring.domain.enums.BlogType;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author hp
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = { MyTestConfiguration.class })
@EnableTransactionManagement
public class DaoImplIntegrationTest {
  
    @Resource
    private BlogDao dao;
    
    @Before
    public void setup() {
        final Blog blog = new Blog();
        blog.setDescription("Sample Blog Description " + 1);
        blog.setEnabled(true);
        blog.setHandle("Sample Blog Handle " + 1);
        blog.setId(1);
        blog.setTimeCreated(new Date());
        blog.setType(BlogType.MOVIES);
        dao.save(blog);
    }
  
// Still giving problem    
//    @Test
    public void givenBlogs_whenFindAll_thenReturnAll(){
        final List<Blog> blogs = dao.findAll();
        assertEquals("size incorrect", 1, blogs.size());        
    }
}