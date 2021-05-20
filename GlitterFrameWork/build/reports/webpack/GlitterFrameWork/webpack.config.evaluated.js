{
  mode: 'production',
  resolve: {
    modules: [
      '/Users/jianzhi.wang/Desktop/ktor_framework/Glitter/GlitterFrameWork/build/js/packages/GlitterFrameWork/kotlin-dce',
      'node_modules'
    ]
  },
  plugins: [
    ProgressPlugin {
      profile: false,
      handler: [Function: handler],
      modulesCount: 500,
      showEntries: false,
      showModules: true,
      showActiveModules: true
    },
    TeamCityErrorPlugin {}
  ],
  module: {
    rules: [
      {
        test: /\.js$/,
        use: [
          'source-map-loader'
        ],
        enforce: 'pre'
      }
    ]
  },
  entry: {
    main: [
      '/Users/jianzhi.wang/Desktop/ktor_framework/Glitter/GlitterFrameWork/build/js/packages/GlitterFrameWork/kotlin-dce/GlitterFrameWork.js'
    ]
  },
  output: {
    path: '/Users/jianzhi.wang/Desktop/ktor_framework/Glitter/GlitterFrameWork/build/distributions',
    filename: [Function: filename],
    library: 'GlitterFrameWork',
    libraryTarget: 'umd',
    globalObject: 'this'
  },
  devtool: 'source-map',
  stats: {
    warningsFilter: [
      /Failed to parse source map/
    ],
    warnings: false,
    errors: false
  }
}