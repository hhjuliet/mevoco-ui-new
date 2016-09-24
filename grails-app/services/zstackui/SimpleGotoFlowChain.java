package zstackui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

/**
 * Created by hhjuliet on 2016/9/24.
 */
public abstract class SimpleGotoFlowChain implements FlowTrigger,FlowRollback  {
	/*private HashMap<String,Flow> flows= new HashMap<>();
	private Stack<Flow> rollbackFlows = new Stack<Flow>();
	private Iterator<Flow> it;
	private Boolean isStart = false;
	private Boolean isRollback = false;
	private Flow currentFlow;
	private Flow currentRollbackFlow;

	abstract void done();
	abstract void setup();

	@Override
	public void next(String nextStep) {
		if (!isStart){
			System.out.println("you must call start first");
			return;
		}
		if (isRollback){
			System.out.println("rollback has start,you cannot call next()");
			return;
		}
		rollbackFlows.push(currentFlow);

		if (flows.get(nextStep) == null){
			System.out.println("step not exist!");
			return;
		}

		Flow flow = flows.get(nextStep);
		runFlow(flow);
	}

	@Override
	public void rollback(){
		isRollback = true;
		if (rollbackFlows.empty()){
			System.out.println("rollback cannot be called while donnot need rollback");
			return;
		}

		Flow flow = rollbackFlows.pop();
		currentRollbackFlow = flow;
		rollbackFlow(flow);
	}

	@Override
	public void failed(String errorCode){
		System.out.println("error code is :"+errorCode);
		rollbackFlows.push(currentFlow);
		rollback();
	}

	public void start(){
		setup();
		isStart = true;
		if (flows.isEmpty()){
			System.out.println("no flow in flows");
			return;
		}
		Flow flow = flows.get("step0");
		runFlow(flow);
	}

	public void addFlow(String flowId,Flow Flow){
		flows.put(flowId,Flow);
	}

	private void runFlow(Flow flow){
		currentFlow = flow;
		flow.run(this);
	}

	private void rollbackFlow(Flow flow){
		flow.rollback(this);
	}*/

}
