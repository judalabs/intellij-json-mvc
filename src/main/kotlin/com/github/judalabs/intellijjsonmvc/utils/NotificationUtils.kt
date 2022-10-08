package com.github.judalabs.intellijjsonmvc.utils

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

internal object NotificationUtils {
    fun jsonGenerated(project: Project?, fileName: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("JSON-generated")
            .createNotification("JSON generated for class $fileName", NotificationType.INFORMATION)
            .notify(project)
    }
}