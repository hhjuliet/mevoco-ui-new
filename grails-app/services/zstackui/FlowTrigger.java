package zstackui;

/**
 * Created by hhjuliet on 2016/9/23.
 */
interface FlowTrigger {
	void next(String nextstep);
	void failed(String errorCode);
}
