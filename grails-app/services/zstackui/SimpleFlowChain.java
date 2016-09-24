package zstackui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Created by hhjuliet on 2016/9/23.
 */
/*public abstract class SimpleFlowChain implements FlowRollback,FlowTrigger{
	private List<Flow> flows= new ArrayList<Flow>();
	private Stack<Flow> rollbackFlows = new Stack<Flow>();
	private Iterator<Flow> it;
	private Boolean isStart = false;
	private Boolean isRollback = false;
	private Flow currentFlow;
	private Flow currentRollbackFlow;

	abstract void done();
	abstract void setup();

	@Override
	public void next() {
		if (!isStart){
			System.out.println("you must call start firt");
			return;
		}
		if (isRollback){
			System.out.println("rollback has start,you cannot call next()");
			return;
		}
		rollbackFlows.push(currentFlow);

		if (!it.hasNext()){
			done();
			return;
		}

		Flow flow = it.next();
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
		it = flows.iterator();
		Flow flow = it.next();
		runFlow(flow);
	}

	public void addFlow(Flow flow){
		flows.add(flow);
	}

	private void runFlow(Flow flow){
		currentFlow = flow;
		flow.run(this);
	}

	private void rollbackFlow(Flow flow){
		flow.rollback(this);
	}

}*/
