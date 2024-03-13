package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IBlogService extends IService<Blog> {

    Result queryBlogById(Long id);

    Result queryHotBlog(Integer current);

    Result likeblog(Long id);

    Result queryBloglikes(Long id);

    Result saveBlog(Blog blog);

    Result queryBlogOfFellow(Long max, Integer offset);
}
