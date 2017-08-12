/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2017 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.gradle.support;

import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;

import com.google.common.collect.Lists;

public class JavaSupport extends AbstractSupport {

	@Override
	public void apply(Project project) {
		project.getPlugins().apply(JavaPlugin.class);

		project.getConfigurations().all(config -> {
			config.resolutionStrategy(strategy -> {
				strategy.preferProjectModules();
			});
		});

		project.getTasks().withType(JavaCompile.class).all(task -> {
			if (task.getOptions().getEncoding() == null) {
				task.getOptions().setEncoding("UTF-8");
			}
		});

		final JavaPluginConvention convention = project.getConvention().getPlugin(JavaPluginConvention.class);
		final SourceSet main = convention.getSourceSets().findByName(SourceSet.MAIN_SOURCE_SET_NAME);
		final SourceSet test = convention.getSourceSets().findByName(SourceSet.TEST_SOURCE_SET_NAME);

		// force groovy compiler
		if (project.getPlugins().hasPlugin(GroovyPlugin.class)) {
			final GroovySourceSet mainGroovy = new DslObject(main).getConvention().getPlugin(GroovySourceSet.class);
			final GroovySourceSet testGroovy = new DslObject(test).getConvention().getPlugin(GroovySourceSet.class);
			mainGroovy.getGroovy().srcDirs(main.getJava().getSrcDirs());
			testGroovy.getGroovy().srcDirs(test.getJava().getSrcDirs());
			main.getJava().setSrcDirs(Lists.newArrayList());
			test.getJava().setSrcDirs(Lists.newArrayList());
		}

		// make sure to include non-java resources from src/main/java
		main.getJava().getSrcDirs().forEach( dir -> {
			main.resources(res -> {
				res.srcDir(dir);
				res.exclude("**/*.java");
				res.exclude("**/*.groovy");
			});
		});
		test.getJava().getSrcDirs().forEach( dir -> {
			test.resources(res -> {
				res.srcDir(dir);
				res.exclude("**/*.java");
				res.exclude("**/*.groovy");
			});
		});
	}
}