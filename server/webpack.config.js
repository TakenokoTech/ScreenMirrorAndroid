const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const entry = {
    main: './src/main',
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
    // new HtmlWebpackPlugin({ template: './static/index.html' }),
    // new CopyWebpackPlugin([{ from: '.', to: '.', ignore: ['!*.html'] }], { context: 'static' }),
    // new CopyWebpackPlugin([{ from: '.', to: './css', ignore: ['!*.css'] }], { context: 'static/css' }),
    // new CopyWebpackPlugin([{ from: ".", to: "./js", ignore: ["!*.js"] }], { context: "static/js" }),
    // new CopyWebpackPlugin([{ from: '.', to: './assets', ignore: ['!*'] }], { context: 'static/assets' }),
    // new CopyWebpackPlugin([{ from: ".", to: "./", ignore: ["!sw.js"] }], { context: "static/" }),
    // new CopyWebpackPlugin([{ from: '.', to: './', ignore: ['!manifest.json'] }], { context: 'static/' }),
];

const development = {
    entry: entry,
    target: 'electron-main',
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: '[name].js?[hash]',
    },
    resolve: { extensions: ['.tsx', '.ts', '.js', '.json'] },
    module: { rules: [babelRule, cssRule] },
    plugins: plugins,
    devtool: 'inline-source-map',
    devServer: {
        // host: '0.0.0.0',
        disableHostCheck: true,
    },
};

const production = {
    mode: 'production',
    entry: entry,
    target: 'electron-main',
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: '[name].js?[hash]',
    },
    resolve: { extensions: ['.tsx', '.ts', '.js', '.json'] },
    module: { rules: [babelRule, cssRule] },
    plugins: plugins,
};

if ((process.env.NODE_ENV || '').trim() != 'production') {
    console.log('NODE_ENV', 'development');
    module.exports = development;
} else {
    console.log('NODE_ENV', 'production');
    module.exports = production;
}
