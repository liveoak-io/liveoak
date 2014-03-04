'use strict';

module.exports = function (grunt) {
  // load all grunt tasks
  require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

  // configurable paths
  var projectConfig = {
    src: 'app',
    dist: '../../../target/app'
  };

  grunt.initConfig({
    config: projectConfig,
    less: {
      development: {
        options: {
          paths: ['../../../target/app/css/']
        },
        files: {
          '../../../target/app/css/console.css': 'app/less/console.less'
        }
      }
    },
    watch: {
      options: {
        livereload: true
      },
      css: {
        files: 'app/less/*.less',
        tasks: ['less']
      },
      js: {
        files: ['app/js/*.js','app/js/controllers/*.js'],
        tasks: ['copy','jshint']
      },
      html: {
        files: ['app/partials/*.html',
          'app/templates/*.html',
          'app/*.html'],
        tasks: ['copy']
      },
      livereload: {
        files: [
          'app/*.html',
          'app/partials/*.html',
          'app/templates/*.html',
          'js/*.js'
        ]
      }
    },
    copy: {
      build: {
        cwd: '',
        src: [ 'app/js/**', 'app/img/**', 'app/css/**', 'app/lib/**', 'app/partials/**', 'app/templates/**', 'app/*.html' ],
        dest: '../../../target/',
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
        'app/js/{,*/}*.js'
      ],
      test: {
        options: {
          jshintrc: 'test/.jshintrc'
        },
        src: ['test/{,*/}*.js']
      }
    }
  });

  grunt.registerTask('build', [
    'less','jshint','copy'
  ]);

  grunt.registerTask('default', ['build', 'watch']);
};
