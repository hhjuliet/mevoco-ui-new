package zstackui;

/**
 * Created by hhjuliet on 2016/9/23.
 */
public interface FlowTrigger {
	void next();
	void failed(String errorCode);
}
