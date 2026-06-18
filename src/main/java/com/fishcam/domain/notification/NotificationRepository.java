package com.fishcam.domain.notification;

import com.fishcam.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {




    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndReadFalse(User user);

    // recent: on réutilise Pageable pour limiter (limit=5)
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    Page<Notification> findRecentByUser(@Param("user") User user, Pageable pageable);

    // mark all as read en 1 requête SQL
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user AND n.read = false")
    int markAllAsReadByUser(@Param("user") User user);

    boolean existsByUserAndMessageContainingAndType(User user, String message, TypeNotification type);
}