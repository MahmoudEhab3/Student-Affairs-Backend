package com.unilink.repository;

import com.unilink.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByTimestampDesc(Integer userId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.id > :lastId ORDER BY n.timestamp DESC")
    List<Notification> findNewNotifications(@Param("userId") Integer userId, @Param("lastId") Long lastId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = 'Read' WHERE n.id = :id AND n.userId = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Integer userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = 'Unread'")
    long countUnreadByUserId(@Param("userId") Integer userId);
}