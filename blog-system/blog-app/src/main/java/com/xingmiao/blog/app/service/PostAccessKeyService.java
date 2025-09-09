package com.xingmiao.blog.app.service;

import com.xingmiao.blog.common.dto.PostAccessKeyItem;
import com.xingmiao.blog.common.dto.PostPasswordsUpdateRequest;

import java.util.List;

public interface PostAccessKeyService {
    void updatePostPasswords(Long postId, PostPasswordsUpdateRequest request);
    List<PostAccessKeyItem> listPostPasswords(Long postId);
    boolean validatePassword(Long postId, String rawPassword);
    Long matchKeyId(Long postId, String rawPassword);
}


