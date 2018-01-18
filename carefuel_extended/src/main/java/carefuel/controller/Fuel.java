/**
 *
 */
package carefuel.controller;

/**
 * @author Wolfgang
 *
 */
public enum Fuel {
	DIESEL, E5, E10;
	
	
	public static int getDefaultPrice(Fuel fuel) {
		switch(fuel) {
		case DIESEL:
			return 1150;
		case E5:
			return 1250;
		case E10:
			return 1300;
		default:
			return Integer.MAX_VALUE;
		}
	}
}