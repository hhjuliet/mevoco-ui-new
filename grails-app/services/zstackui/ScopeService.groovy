package zstackui

abstract class ScopeService {
    List<Runner> steps = new ArrayList<>();

    abstract void error(int i)

    abstract void done()

    abstract void setup();

    void addStep(Runner service) {
        steps.add(service);
    }


    void runStep(ListIterator it) {
        if (!it.hasNext()) {
            println "finish"
            done()
            return
        }

        Runner runner = it.next();
        runner.run(new Callback() {
            @Override
            void success() {
                runStep(it);
            }

            @Override
            void failed(int i) {
                println "error: " + i
                error(i);
            }
        })

    }

	void rollbackStep(ListIterator it){
		if (!it.hasPrevious()){
			println "rollback finished!"
		}

		Runner runner = it.previous();
		runner.rollback(new Callback() {
			@Override
			void success() {
				rollbackStep(it);
			}

			@Override
			void failed(int i) {

			}
		})
	}

    void start() {
        setup()
        ListIterator<Runner> it = steps.listIterator();
        runStep(it)
    }

}
