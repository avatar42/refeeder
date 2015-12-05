package dea.refeeder;

public class Utf8ToAscii {

	public static void main(String[] args) {
		String[] a = { "’", "\'", "�?", "\"", "…", "...", "–", "--",
				"“", "\"" };
		for (int i = 0; i < a.length; i++) {
			System.out.print(a[i] + "(");
			String s = a[i];
			for (int c = 0; c < a[i].length(); c++) {
				char f = s.charAt(c);
				System.out.print(Integer.toHexString(f));
			}
			System.out.println(")");
		}
		System.out.println("’ -> \'");
		System.out.println("�? -> \"");
		System.out.println("… -> ...");
		System.out.println("– -> --");
		System.out.println("“ -> \"");
	}
}
