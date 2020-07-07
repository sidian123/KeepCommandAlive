package live.sidian.command.keepalive;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;

/**
 * 任务调度器, 用于处理相同类型的, 具有时效性的任务.
 * <p></p>
 * 规则如下
 * <ul>
 *     <li>队列内为空时, 先入队的任务立刻被执行</li>
 *     <li>已有任务执行期间, 队列中仅留下最新到来的任务</li>
 *     <li>两个任务执行之间有一定延迟</li>
 * </ul>
 * @author Sidian.Luo
 * @date 2019/12/17 16:18
 */
public class MyTaskScheduler implements Executor {
    /**
     * 队列
     */
    ArrayBlockingQueue<Runnable> queue=new ArrayBlockingQueue<>(1);

    /**
     * 消费者线程, 用于处理任务
     */
    Thread consumer;

    /**
     * 队列中任务执行之间的延迟, 单位ms
     */
    long delay;

    /**
     * 默认delay 300ms
     */
    public MyTaskScheduler(){
        this(300);
    }

    /**
     * @param delay 任务执行之间的延迟,单位ms
     */
    public MyTaskScheduler(long delay){
        //初始化
        this.delay=delay;
        //启动队列的消费者
        consumer=new Thread(this::run);
        consumer.start();
        //注册资源清理事件
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("VM关闭, 即将关闭调度器");//不能使用log重量级框架打印日志, 否则将无输出
                MyTaskScheduler.this.close();
            }
        });
    }

    /**
     * 添加到队列中, 等待消费者执行.
     * @param task 任务
     */
    @Override
    public void execute(Runnable task) {
        //废弃队列中过时任务
        queue.clear();
        //入队新任务
        queue.add(task);
    }

    /**
     * 消费队列, 执行元素代表的任务
     */
    private void run() {
        while(true){
            try {
                //执行任务
                queue.take().run();
            } catch (InterruptedException e) {
                //退出线程
                System.out.println("调度器的消费线程已关闭");
                return;
            }
        }
    }

    /**
     * 关闭资源, 如线程
     */
    public void close(){
        consumer.interrupt();
    }

    /**
     * 资源清理
     */
    @Override
    protected void finalize() {
        close();
    }
}
