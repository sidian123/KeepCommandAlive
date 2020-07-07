package live.sidian.command.keepalive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@SpringBootApplication
public class KeepAliveApplication implements ApplicationContextAware{


    public static void main(String[] args) {
        SpringApplication.run(KeepAliveApplication.class, args);
    }

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
        while(true){
            try {
                log.info("执行命令:"+command.getRun());
                defaultExecutor.execute(CommandLine.parse(command.getRun()));
                log.info("命令正常结束");
                break;
            } catch (ExecuteException e){
                log.warn("命令运行异常, exit:"+e.getExitValue());
                log.info("将重新运行命令");
                sleep();
            } catch (IOException e) {
                log.error("未知异常");
                e.printStackTrace();
                break;
            }
        }
        // 退出程序
        SpringApplication.exit(this.applicationContext,() -> 0);
     }

     void sleep(){
         try {
             Thread.sleep(2000);
         } catch (InterruptedException ignored) {}
     }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
}
