module.exports = (grunt) ->

	config =
		watch:
			all:
				files: ['./src/**/*.jade', './src/**/*.coffee']
				tasks: ['clear', 'jade:compile', 'coffee:compile', 'uglify:minify']
				options:
					livereload: true
		jade:
			compile:
				files:
					'./index.html': ['./src/index.jade']
		coffee:
			compile:
				files:
					'./temp/scaling.js': ['./src/scaling.coffee']
		uglify:
			minify:
				files:
					'./scaling.min.js': ['./temp/scaling.js']
					'./ext/jquery.stuck.min.js': ['./ext/jquery.stuck.js']

	grunt.initConfig config

	for task in ['grunt-clear', 'grunt-contrib-jade', 'grunt-contrib-watch', 'grunt-contrib-coffee',
		'grunt-contrib-uglify']
		grunt.loadNpmTasks task

	grunt.registerTask 'default', ['watch']
