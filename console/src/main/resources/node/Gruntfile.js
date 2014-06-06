module.exports = function(grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        bower: {
            install: {
                options: {
                    cleanTargetDir: true,
                    cleanBowerDir: true,
                    targetDir: "app/lib",
                    verbose: true
                }
            }
        }    ,
      copy: {
        fa: {
          cwd: 'app/lib/font-awesome/fonts',
          src: '*',
          dest: 'app/lib/patternfly/components/font-awesome/fonts',
          expand: true
        },
        glyph: {
          cwd: 'app/lib/bootstrap',
          src: 'glyph*',
          dest: 'app/lib/patternfly/components/bootstrap/dist/fonts',
          expand: true
        }
      }
    });
    grunt.loadNpmTasks('grunt-bower-task');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-mkdir');
    grunt.registerTask('build', ['bower', 'copy']);
};
