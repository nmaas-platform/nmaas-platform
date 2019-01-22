const puppeteer = require('puppeteer');
process.env.CHROME_BIN = puppeteer.executablePath();

module.exports = function (config) {
    config.set({
        basePath: '',
        frameworks: ['jasmine', '@angular-devkit/build-angular'],
        plugins: [
            require('karma-jasmine'),
            require('karma-babel-preprocessor'),
            require('karma-chrome-launcher'),
            require('karma-webpack'),
            require('karma-jasmine-html-reporter'),
            require('karma-coverage-istanbul-reporter'),
            require('@angular-devkit/build-angular/plugins/karma'),
        ],
        customLaunchers: {
            ChromeHeadless: {
                base: 'Chrome',
                flags: [
                    '--headless',
                    '--disable-gpu',
                    // Without a remote debugging port, Google Chrome exits immediately.
                    '--remote-debugging-port=9222',
                ],
            }
        },
        client:{
            clearContext: false
        },
        files: [
            
        ],
        preprocessors: {},
        mime: {
            'text/x-typescript': ['ts','tsx']
        },
        coverageIstanbulReporter: {
            dir: require('path').join(__dirname, 'coverage'), reports: [ 'html', 'lcovonly' ],
            fixWebpackSourcePaths: true
        },
        
        reporters: ['progress', 'coverage-istanbul'],
//        reporters: config.angularCli && config.angularCli.codeCoverage
//            ? ['progress', 'coverage-istanbul']
//            : ['progress', 'kjhtml'],
        port: 9876,
        colors: true,
        logLevel: config.LOG_WARN,
        autoWatch: false,
        browsers: ['Chrome', 'ChromeHeadless'],
        singleRun: false
    }
    );
};
