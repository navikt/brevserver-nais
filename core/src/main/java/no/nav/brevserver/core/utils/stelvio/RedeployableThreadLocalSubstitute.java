package no.nav.brevserver.core.utils.stelvio;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class RedeployableThreadLocalSubstitute<T> {
	private final  Map<Thread, T> valuesByThread = Collections.synchronizedMap(new WeakHashMap());

	public RedeployableThreadLocalSubstitute() {
	}

	public boolean isValueSet() {
		return this.get() != null;
	}

	public T get() {
		return this.valuesByThread.get(Thread.currentThread());
	}

	public void set(T value) {
		if (value == null) {
			this.remove();
		} else {
			this.valuesByThread.put(Thread.currentThread(), value);
		}

	}

	public void remove() {
		this.valuesByThread.remove(Thread.currentThread());
	}
}
