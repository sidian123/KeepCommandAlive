package live.sidian.command.keepalive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@SpringBootApplication
public class KeepAliveApplication {


    public static void main(String[] args) {
        SpringApplication.run(KeepAliveApplication.class, args);
    }

    private final MyTaskScheduler taskScheduler=new MyTaskScheduler(5000);

    @Resource
    Command command;

    ApplicationContext applicationContext;


    @PostConstruct
    void init(){
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        // 设置正常结束状态码
        int[] exits = command.getExits();
        if(exits.length!=0){
            defaultExecutor.setExitValues(exits);
        }
        // 设置输入输出
        defaultExecutor.setStreamHandler(new PumpStreamHandler());
        // 挂载VM结束事件
        defaultExecutor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        // 运行命令
        try {
            log.info("执行命令:"+command.getRun());
            defaultExecutor.execute(CommandLine.parse(command.getRun()));
            log.info("命令正常结束");
        } catch (ExecuteException e){
            log.warn("命令运行异常, exit:"+e.getExitValue());
            log.info("将重新运行命令");
            taskScheduler.execute(KeepAliveApplication.this::init);
        } catch (IOException e) {
            log.error("未知异常");
            e.printStackTrace();
        }
        // 退出程序
        System.exit(0);
     }


}
