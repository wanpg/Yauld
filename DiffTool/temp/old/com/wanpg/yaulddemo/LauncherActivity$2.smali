.class Lcom/wanpg/yaulddemo/LauncherActivity$2;
.super Ljava/lang/Object;
.source "LauncherActivity.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/wanpg/yaulddemo/LauncherActivity;->toMainActivity()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/wanpg/yaulddemo/LauncherActivity;


# direct methods
.method constructor <init>(Lcom/wanpg/yaulddemo/LauncherActivity;)V
    .registers 2
    .param p1, "this$0"    # Lcom/wanpg/yaulddemo/LauncherActivity;

    .prologue
    .line 31
    iput-object p1, p0, Lcom/wanpg/yaulddemo/LauncherActivity$2;->this$0:Lcom/wanpg/yaulddemo/LauncherActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .registers 3

    .prologue
    .line 34
    new-instance v0, Landroid/content/Intent;

    invoke-direct {v0}, Landroid/content/Intent;-><init>()V

    .line 35
    .local v0, "intent":Landroid/content/Intent;
    const-string v1, "com.wanpg.yaulddemo.MainActivity"

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setAction(Ljava/lang/String;)Landroid/content/Intent;

    .line 36
    iget-object v1, p0, Lcom/wanpg/yaulddemo/LauncherActivity$2;->this$0:Lcom/wanpg/yaulddemo/LauncherActivity;

    invoke-virtual {v1}, Lcom/wanpg/yaulddemo/LauncherActivity;->getPackageName()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;

    .line 37
    iget-object v1, p0, Lcom/wanpg/yaulddemo/LauncherActivity$2;->this$0:Lcom/wanpg/yaulddemo/LauncherActivity;

    invoke-virtual {v1, v0}, Lcom/wanpg/yaulddemo/LauncherActivity;->startActivity(Landroid/content/Intent;)V

    .line 38
    return-void
.end method
