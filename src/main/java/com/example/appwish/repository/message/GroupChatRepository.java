package com.example.appwish.repository.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.appwish.model.message.GroupChat;

@Repository
public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {
    // 基本的なCRUD操作はJpaRepositoryによって提供されます
    // 必要に応じて、カスタムクエリメソッドをここに追加できます
}