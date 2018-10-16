package carmelo.examples.client;

import java.util.concurrent.TimeUnit;

//import com.netty.im.client.ImClientApp;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
/**
 * 负责监听启动时连接失败，重新连接功能
 * @author yinjihuan
 *https://blog.csdn.net/z69183787/article/details/52671543
 */
public class ConnectionListener implements ChannelFutureListener {
	
	private TcpClientMain client = TcpClientMain.getInstance();
	
	@Override
	public void operationComplete(ChannelFuture channelFuture) throws Exception {
		if (!channelFuture.isSuccess()) {
			final EventLoop loop = channelFuture.channel().eventLoop();
			loop.schedule(new Runnable() {
				@Override
				public void run() {
					System.err.println("服务端链接不上，开始重连操作...");
					client.connect(TcpClientMain.getHost(), TcpClientMain.getPort());
				}
			}, 1L, TimeUnit.SECONDS);
		} else {
			System.err.println("服务端链接成功...");
		}
	}
}