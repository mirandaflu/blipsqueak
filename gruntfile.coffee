module.exports = (grunt) ->

	config =
		watch:
			all:
				files: ['./src/**/*.jade', './src/**/*.coffee']
				tasks: ['clear', 'jade:compile', 'coffee:compile']
				options:
					livereload: true
		jade:
			compile:
				files:
					'./index.html': ['./src/index.jade']
		coffee:
			compile:
				files:
					'./scaling.js': ['./src/scaling.coffee']

	grunt.initConfig config

	for task in ['grunt-clear', 'grunt-contrib-jade', 'grunt-contrib-watch', 'grunt-contrib-coffee']
		grunt.loadNpmTasks task

	grunt.registerTask 'default', ['watch']
