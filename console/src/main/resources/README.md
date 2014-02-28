# LiveOak Console Grunt Integration

## Why Grunt?

Grunt tasks configured in this project greatly increase the developing comfort and help to identify JS and LESS related
bugs in real-time.

Currently, these tasks are provided:

* less - Compile LESS files and copy them to the target directory.
* copy - Copies project related files (images, html, css, 3rd party libraries) to the target directory.
* jshint - Checks the JS code to identify errors.
* watch - Watches your code live + run relevant tasks on code change (less, copy, jshint).
* build - Runs less, jshint and copy tasks.

## Getting started

You have to install required software before you're able to use grunt:

* Install npm - Find more information on https://www.npmjs.org/
* Install grunt - Find more information on http://gruntjs.com/
* Install npm dependencies with:
```shell
npm install
```

## LO Console Grunt How-to

Before pushing, please check your code for errors. If there are no errors, the build task should success. Run the
build task with:
```shell
grunt build
```

When developing, run the grunt default tasks to watch your code for changes. After each change (file save) in all
relevant project files, they get compiled (if needed, i.e. the LESS files must be compiled) and copied to the relevant
target directory. The JS code is checked for errors and project code-style practices. To run the default task, simply
run the grunt command:
```shell
grunt
```