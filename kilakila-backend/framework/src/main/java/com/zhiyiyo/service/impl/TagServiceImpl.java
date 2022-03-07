package com.zhiyiyo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhiyiyo.constants.SystemConstants;
import com.zhiyiyo.domain.ResponseResult;
import com.zhiyiyo.domain.entity.Article;
import com.zhiyiyo.domain.entity.ArticleTag;
import com.zhiyiyo.domain.entity.Tag;
import com.zhiyiyo.domain.vo.TagVo;
import com.zhiyiyo.mapper.ArticleTagMapper;
import com.zhiyiyo.mapper.TagMapper;
import com.zhiyiyo.service.ArticleService;
import com.zhiyiyo.service.TagService;
import com.zhiyiyo.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Override
    public ResponseResult getTagList() {
        // 查询出所有非草稿文章携带的标签 id
        List<Article> articles = articleService.listNormalArticle();
        List<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toList());
        QueryWrapper<ArticleTag> articleTagWrapper = new QueryWrapper<>();
        articleTagWrapper.in("article_id", articleIds);
        articleTagWrapper.select("tag_id", "count(*) as count").groupBy("tag_id");
        List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagWrapper);

        // 获取标签名和标签出现次数
        Map<Long, Integer> tagCountMap = articleTags.stream()
                .collect(Collectors.toMap(ArticleTag::getTagId, ArticleTag::getCount));
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Tag::getId, tagCountMap.keySet());
        List<TagVo> tagVos = BeanCopyUtils.copyBeanList(list(wrapper), TagVo.class);
        tagVos.forEach(tagVo -> tagVo.setCount(tagCountMap.get(tagVo.getId())));

        return ResponseResult.okResult(tagVos);
    }
}