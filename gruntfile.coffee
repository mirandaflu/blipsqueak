module.exports = (grunt) ->

	config =
		watch:
			all:
				files: ['./src/**/*.jade', './src/**/*.coffee']
				tasks: ['clear', 'jade:compile', 'htmlmin:minify', 'coffee:compile', 'uglify:minify']
				options:
					livereload: true
		jade:
			compile:
				files:
					'./temp/index.html': ['./src/index.jade']
					'./temp/gallery.html': ['./src/gallery.jade']
		coffee:
			compile:
				files:
					'./temp/scaling.js': ['./src/scaling.coffee']
		uglify:
			minify:
				files:
					'./scaling.min.js': ['./temp/scaling.js']
					'./ext/jquery.stuck.min.js': ['./ext/jquery.stuck.js']
		htmlmin:
			minify:
				files:
					'./index.html': ['./temp/index.html']
					'./gallery.html': ['./temp/gallery.html']
				options:
					removeComments: true
					collapseWhitespace: true

	grunt.initConfig config

	for task in ['grunt-clear', 'grunt-contrib-jade', 'grunt-contrib-watch', 'grunt-contrib-coffee',
		'grunt-contrib-uglify', 'grunt-contrib-htmlmin']
		grunt.loadNpmTasks task

	grunt.registerTask 'default', ['watch']
