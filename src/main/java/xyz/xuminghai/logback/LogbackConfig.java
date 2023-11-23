package xyz.xuminghai.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.ContextAwareBase;
import org.slf4j.Logger;

/**
 * 2022/3/1 20:49 星期二<br/>
 * 基于SPI的logback配置
 *
 * @author xuMingHai
 */
public class LogbackConfig extends ContextAwareBase implements Configurator {

    @Override
    public ExecutionStatus configure(LoggerContext loggerContext) {
        // 开始自定义实现类配置
        addInfo("开始自定义配置实现类");

        // 控制台附加器
        final ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        // 设置上下文
        consoleAppender.setContext(loggerContext);
        // 设置附加器名字
        consoleAppender.setName("root");

        // 字符集
        final LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
        encoder.setContext(loggerContext);

        // 设置带有颜色的图案布局
        final PatternLayout patternLayout = new PatternLayout();
        patternLayout.setContext(loggerContext);
        patternLayout.setPattern("%d %highlight(%-5level) [%thread] --- %cyan(%logger{25}) : %msg %n");
        // 激活布局（完成设置）
        patternLayout.start();

        // 编码设置布局
        encoder.setLayout(patternLayout);
        // 激活编码
        encoder.start();

        // 控制台附加器设置编码
        consoleAppender.setEncoder(encoder);
        // 激活此附加器
        consoleAppender.start();

        // 在root记录器添加附加器
        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(consoleAppender);

        return ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY;
    }

}
