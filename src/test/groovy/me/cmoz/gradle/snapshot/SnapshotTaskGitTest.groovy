/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.cmoz.gradle.snapshot

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

class SnapshotTaskGitTest {

    private Project project

    @Before
    void setUp() {
        project = new ProjectBuilder()
                .withProjectDir(new File("src/test/resources/testGit"))
                .build()
        new SnapshotPlugin().apply(project)

        // workaround because Git won't version control a '.git' folder.
        project.file('.git').mkdirs()
        project.copy {
            from('_git')
            into('.git')
        }
    }

    @After
    void tearDown() {
        project.delete('.git')
        project.delete(project.buildDir)
    }

    @Test
    void "Task generates snapshot file with Git project"() {
        def task = project.tasks.getByName(SnapshotPlugin.SNAPSHOT_TASK_NAME)
        task.actions.each { final Action action ->
            action.execute(task)
        }

        def properties = new Properties()
        def outputDir = new File(project.buildDir, "snapshot")
        def snapshot = new File(outputDir, SnapshotPluginExtension.DEFAULT_FILENAME)
        properties.load(new FileReader(snapshot))

        assertEquals("d38aade", properties.get(Commit.ID_ABBREV));
        assertEquals("d38aade25e358ee9ce436e665e321a09d99c041e", properties.get(Commit.ID))
        assertEquals("chris@cmoz.me", properties.get(Commit.USER_EMAIL))
        assertEquals("Test commit.", properties.get(Commit.MESSAGE_FULL))
        assertEquals("Test commit", properties.get(Commit.MESSAGE_SHORT))
        assertEquals("Chris Molozian", properties.get(Commit.USER_NAME))
        assertEquals("master", properties.get(Commit.BRANCH))
        assertEquals("chris@cmoz.me", properties.get(Commit.BUILD_USER_EMAIL))
        assertEquals("Chris Molozian", properties.get(Commit.BUILD_USER_NAME))
    }

    @Test
    void "Task inserts commit properties with Git project"() {
        def task = project.tasks.getByName(SnapshotPlugin.SNAPSHOT_TASK_NAME)
        task.actions.each { final Action action ->
            action.execute(task)
        }

        def properties = project.getProperties();
        assertEquals("d38aade", properties.get(Commit.ID_ABBREV));
        assertEquals("d38aade25e358ee9ce436e665e321a09d99c041e", properties.get(Commit.ID))
        assertEquals("chris@cmoz.me", properties.get(Commit.USER_EMAIL))
        assertEquals("Test commit.", properties.get(Commit.MESSAGE_FULL))
        assertEquals("Test commit", properties.get(Commit.MESSAGE_SHORT))
        assertEquals("Chris Molozian", properties.get(Commit.USER_NAME))
        assertEquals("master", properties.get(Commit.BRANCH))
        assertEquals("chris@cmoz.me", properties.get(Commit.BUILD_USER_EMAIL))
        assertEquals("Chris Molozian", properties.get(Commit.BUILD_USER_NAME))
    }

}
