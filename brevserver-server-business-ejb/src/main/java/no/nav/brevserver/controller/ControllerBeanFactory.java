package no.nav.brevserver.controller;

/**
 * Factory for obtaining an instance of {@link ControllerBi}.
 *
 * @author Marius Thåring, Visma Consulting
 */
public enum ControllerBeanFactory {

	INSTANCE;

	private ControllerBi controller;


	private ControllerBeanFactory() {
		controller = new ControllerBean();
	}

	/**
	 * Convience method.
	 *
	 * @return The INSTANCE of {@link ControllerBeanFactory}.
	 */
	public static ControllerBeanFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Getter for {@link ControllerBi}.
	 *
	 * @return An instance of {@link ControllerBi}.
	 */
	public ControllerBi getController() {
		return controller;
	}
}
