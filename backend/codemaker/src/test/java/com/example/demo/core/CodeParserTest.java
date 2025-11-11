package com.example.demo.core;

import com.example.demo.ai.model.HtmlCodeResult;
import com.example.demo.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeParserTest {

    @Test
    void parseHtmlCode() {
        String codeContent = """
                创建一个完整的网页：
                html 格式
                <!DOCTYPE html>
                <html>
                <head>
                    <title>多文件示例</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                    <h1>欢迎使用</h1>
                    <script src="script.js"></script>
                </body>
                </html>

                css 格式
                h1 {
                    color: blue;
                    text-align: center;
                }
                ```
                ```js
                console.log('页面加载完成');

                文件创建完成！
                """;
        HtmlCodeResult result = CodeParser.parseHtmlCode(codeContent);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
    }

    @Test
    void parseMultiFileCode() {
        String codeContent = """
            一些说明文字，模型有时候会啰嗦一两句。

            ```html
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Demo Page</title>
            </head>
            <body>
            <h1>Hello, Multi File!</h1>
            <div id="app"></div>
            </body>
            </html>
            ```

            ```css
            body {
                font-family: Arial, sans-serif;
                background-color: #f5f5f5;
            }

            h1 {
                color: #333;
            }
            ```

            ```js
            document.addEventListener("DOMContentLoaded", function () {
                const app = document.getElementById("app");
                app.innerText = "JS is working!";
            });
            ```
            """;;
        MultiFileCodeResult result = CodeParser.parseMultiFileCode(codeContent);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
        assertNotNull(result.getCssCode());
        assertNotNull(result.getJsCode());
    }
}
