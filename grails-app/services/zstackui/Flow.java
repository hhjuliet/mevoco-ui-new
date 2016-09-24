package zstackui;

/**
 * Created by hhjuliet on 2016/9/23.
 */
public interface Flow {
	public void run(FlowTrigger trigger);
	public void rollback(FlowRollback trigger);
}
