'use strict';

module.exports = function (grunt) {

  // load all grunt tasks
  grunt.loadNpmTasks('grunt-bower-task');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-connect');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-watch');

  // configurable paths
  var projectConfig = {
    src: '../src/main/resources/app',
    dist: '../../dist/target/liveoak/apps/admin/console/'
  };

  grunt.initConfig({
    config: projectConfig,
    bower: {
      install: {
        options: {
          cleanTargetDir: true,
          cleanBowerDir: false,
          targetDir: 'app/lib',
          verbose: true,
          layout: 'byComponent'
        }
      }
    },
    less: {
      development: {
        options: {
          paths: ['<%= config.dist  %>/css/']
        },
        files: {
          '<%= config.dist %>/css/console.css': '<%= config.src %>/less/console.less',
          '<%= config.dist %>/css/reset.css': '<%= config.src %>/less/reset.less'
        }
      }
    },
    watch: {
      options: {
        livereload: true
      },
      css: {
        files: '<%= config.src %>/less/*.less',
        tasks: ['less']
      },
      js: {
        files: ['<%= config.src %>/js/*.js','<%= config.src %>/js/controllers/*.js'],
        tasks: ['copy','jshint']
      },
      html: {
        files: ['<%= config.src %>/partials/**',
          '<%= config.src %>/templates/**',
          '<%= config.src %>/*.html'],
        tasks: ['copy']
      },
      livereload: {
        files: [
          '<%= config.src %>/*.html',
          '<%= config.src %>/partials/*.html',
          '<%= config.src %>/templates/*.html',
          'js/*.js'
        ]
      }
    },
    copy: {
      build: {
        cwd: '<%= config.src %>',
        src: [ 'js/**', 'img/**', 'css/**', 'lib/**', 'partials/**', 'templates/**', '*.html' ],
        dest: '<%= config.dist %>',
        expand: true
      }
    },
    // Make sure code styles are up to par and there are no obvious mistakes
    jshint: {
      options: {
        jshintrc: '.jshintrc',
        reporter: require('jshint-stylish')
      },
      all: [
        'Gruntfile.js',
        '<%= config.src %>/js/{,*/}*.js'
      ],
      test: {
        options: {
          jshintrc: '<%= config.src %>/../test/.jshintrc'
        },
        src: ['test/{,*/}*.js']
      }
    },
    clean: {
      cache: [
        '.bower-*',
        'bower_components',
        'node',
        'node_modules',
        'app/lib'
      ]
    }
  });

  grunt.registerTask('build', [
    'less','jshint','copy'
  ]);

  grunt.registerTask('default', ['build', 'watch']);
  grunt.registerTask('mvnBuild', ['bower']);
};
