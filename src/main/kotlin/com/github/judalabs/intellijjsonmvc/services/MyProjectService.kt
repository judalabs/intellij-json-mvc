package com.github.judalabs.intellijjsonmvc.services

import com.github.judalabs.intellijjsonmvc.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
