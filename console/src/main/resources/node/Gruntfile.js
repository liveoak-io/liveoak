module.exports = function(grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        bower: {
            install: {
                options: {
                    cleanTargetDir: true,
                    cleanBowerDir: true,
                    targetDir: "@targetDir",
                    verbose: true
                }
            }
        }
    });
    grunt.loadNpmTasks('grunt-bower-task');
    grunt.registerTask('build', ['bower']);
};
