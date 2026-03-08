
    public static void jeiCreate() {
        // 空实现 - 不注册 JEI 面板
    }

    public static void jeiRecipeRegister() {
        // 空实现 - 不注册配方
    }
    
    private static boolean isBlock(String s) {
        return true; // 保留但永远返回 true（阻止所有配方）
    }
}
