class OtherClass {
    public static void main(String[] args) {
        TemplateClass.instrum(2, "Block");
		int x;
		TemplateClass.instrum(3, "Declaration", "OtherClass.main.x");
        MyClass myObj = new MyClass();
		TemplateClass.instrum(4, "Declaration", TemplateClass.pair(myObj, "OtherClass.main.myObj"));
        x = 5;
		TemplateClass.instrum(5, "Assign", TemplateClass.pair(x, "OtherClass.main.x"), 5);
        while (x > 1) {
            TemplateClass.instrum(6, "Block");
			x = x - 1;
			TemplateClass.instrum(7, "Assign", TemplateClass.pair(x, "OtherClass.main.x"), x - 1);
        }
        return;
    }
}