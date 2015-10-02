package cn.liutils.crafting;

/**
 * In a recipe file, use "NAME#DATA*AMOUNT" to specify an element.
 * @author EAirPeter
 */
public class ParsedRecipeElement {

	public String name = null;
	public int data = 0;
	public int amount = 1;

	@Override
	public String toString() {
		return "(" + name + "," + data + "," + amount + ")";
	}
	
}
