.class Lcom/wanpg/yaulddemo/LauncherActivity$1;
.super Ljava/lang/Object;
.source "LauncherActivity.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/wanpg/yaulddemo/LauncherActivity;->onCreate(Landroid/os/Bundle;)V
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
    .line 21
    iput-object p1, p0, Lcom/wanpg/yaulddemo/LauncherActivity$1;->this$0:Lcom/wanpg/yaulddemo/LauncherActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .registers 2

    .prologue
    .line 24
    iget-object v0, p0, Lcom/wanpg/yaulddemo/LauncherActivity$1;->this$0:Lcom/wanpg/yaulddemo/LauncherActivity;

    # invokes: Lcom/wanpg/yaulddemo/LauncherActivity;->toMainActivity()V
    invoke-static {v0}, Lcom/wanpg/yaulddemo/LauncherActivity;->access$000(Lcom/wanpg/yaulddemo/LauncherActivity;)V

    .line 25
    return-void
.end method
