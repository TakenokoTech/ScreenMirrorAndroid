const webpack = require('webpack');
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const electronMainEntry = {
    main: './src/main',
};

const electronRendererEntry = {
    renderer: './src/renderer',
};

const babelRule = {
    test: /\.(ts|js)x?$/,
    exclude: /node_modules/,
    loader: 'babel-loader',
};

const cssRule = {
    test: /\.css/,
    use: [
        'style-loader',
        { loader: 'css-loader', options: { url: false } },
        { loader: 'postcss-loader', options: { sourceMap: true, plugins: [require('autoprefixer')({ grid: true })] } },
    ],
};

const plugins = [
    new HtmlWebpackPlugin({ template: './static/index.html' }),
    // new CopyWebpackPlugin([{ from: '.', to: '.', ignore: ['!*.html'] }], { context: 'static' }),
    new CopyWebpackPlugin([{ from: '.', to: '.', ignore: ['!*.css'] }], { context: 'static' }),
    // new CopyWebpackPlugin([{ from: ".", to: "./js", ignore: ["!*.js"] }], { context: "static/js" }),
    // new CopyWebpackPlugin([{ from: '.', to: './assets', ignore: ['!*'] }], { context: 'static/assets' }),
    // new CopyWebpackPlugin([{ from: ".", to: "./", ignore: ["!sw.js"] }], { context: "static/" }),
    // new CopyWebpackPlugin([{ from: '.', to: './', ignore: ['!manifest.json'] }], { context: 'static/' }),
    new webpack.ProvidePlugin({ $: 'jquery', jQuery: 'jquery' }),
];

const developmentMain = {
    mode: 'development',
    entry: electronMainEntry,
    target: 'electron-main',
    output: { path: path.resolve(__dirname, 'dist'), filename: '[name].js?[hash]' },
    resolve: { extensions: ['.tsx', '.ts', '.js', '.json'] },
    module: { rules: [babelRule, cssRule] },
    devtool: 'inline-source-map',
    devServer: { disableHostCheck: true },
};

const developmentRenderer = {
    mode: 'development',
    entry: electronRendererEntry,
    target: 'electron-renderer',
    output: { path: path.resolve(__dirname, 'dist'), filename: '[name].js?[hash]' },
    resolve: { extensions: ['.tsx', '.ts', '.js', '.json'] },
    module: { rules: [babelRule, cssRule] },
    plugins: plugins,
    devtool: 'inline-source-map',
    devServer: { disableHostCheck: true },
};

const productionMain = {
    mode: 'production',
    entry: electronMainEntry,
    target: 'electron-main',
    output: { path: path.resolve(__dirname, 'dist'), filename: '[name].js?[hash]' },
    resolve: { extensions: ['.tsx', '.ts', '.js', '.json'] },
    module: { rules: [babelRule, cssRule] },
};

const productionRenderer = {
    mode: 'production',
    entry: electronRendererEntry,
    target: 'electron-renderer',
    output: { path: path.resolve(__dirname, 'dist'), filename: '[name].js?[hash]' },
    resolve: { extensions: ['.tsx', '.ts', '.js', '.json'] },
    module: { rules: [babelRule, cssRule] },
    plugins: plugins,
};

const tsServer = {
    mode: 'development',
    entry: { grpc: './src/grpc' },
    target: 'node',
    output: { path: path.resolve(__dirname, 'dist'), filename: '[name].js?[hash]' },
    resolve: { extensions: ['.tsx', '.ts', '.js', '.json'] },
    module: { rules: [babelRule] },
    externals: [require('webpack-node-externals')()],
};

if ((process.env.NODE_ENV || '').trim() != 'production') {
    console.log('NODE_ENV', 'development');
    module.exports = [developmentMain, developmentRenderer, tsServer];
} else {
    console.log('NODE_ENV', 'production');
    module.exports = [productionMain, productionRenderer];
}
