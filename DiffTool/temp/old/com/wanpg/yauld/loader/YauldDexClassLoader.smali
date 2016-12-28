.class public Lcom/wanpg/yauld/loader/YauldDexClassLoader;
.super Ljava/lang/ClassLoader;
.source "YauldDexClassLoader.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/wanpg/yauld/loader/YauldDexClassLoader$DelegateClassLoader;
    }
.end annotation


# instance fields
.field private final delegateClassLoader:Lcom/wanpg/yauld/loader/YauldDexClassLoader$DelegateClassLoader;

.field private isLoadFinish:Z

.field private otherDexesThread:Ljava/lang/Thread;


# direct methods
.method public constructor <init>(Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V
    .registers 11
    .param p1, "original"    # Ljava/lang/ClassLoader;
    .param p2, "nativeLibraryPath"    # Ljava/lang/String;
    .param p3, "codeCacheDir"    # Ljava/lang/String;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/ClassLoader;",
            "Ljava/lang/String;",
            "Ljava/lang/String;",
            "Ljava/util/List",
            "<",
            "Ljava/lang/String;",
            ">;)V"
        }
    .end annotation

    .prologue
    .line 18
    .local p4, "otherDexes":Ljava/util/List;, "Ljava/util/List<Ljava/lang/String;>;"
    invoke-virtual {p1}, Ljava/lang/ClassLoader;->getParent()Ljava/lang/ClassLoader;

    move-result-object v0

    invoke-direct {p0, v0}, Ljava/lang/ClassLoader;-><init>(Ljava/lang/ClassLoader;)V

    .line 19
    const/4 v0, 0x0

    iput-boolean v0, p0, Lcom/wanpg/yauld/loader/YauldDexClassLoader;->isLoadFinish:Z

    .line 20
    invoke-static {p4}, Lcom/wanpg/yauld/loader/YauldDexClassLoader;->createDexPath(Ljava/util/List;)Ljava/lang/String;

    move-result-object v1

    .line 21
    .local v1, "pathBuilder":Ljava/lang/String;
    new-instance v0, Lcom/wanpg/yauld/loader/YauldDexClassLoader$DelegateClassLoader;

    const/4 v5, 0x0

    move-object v2, p3

    move-object v3, p2

    move-object v4, p1

    invoke-direct/range {v0 .. v5}, Lcom/wanpg/yauld/loader/YauldDexClassLoader$DelegateClassLoader;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;Lcom/wanpg/yauld/loader/YauldDexClassLoader$1;)V

    iput-object v0, p0, Lcom/wanpg/yauld/loader/YauldDexClassLoader;->delegateClassLoader:Lcom/wanpg/yauld/loader/YauldDexClassLoader$DelegateClassLoader;

    .line 22
    invoke-direct {p0}, Lcom/wanpg/yauld/loader/YauldDexClassLoader;->onLoadComplete()V

    .line 23
    return-void
.end method

.method private static createDexPath(Ljava/util/List;)Ljava/lang/String;
    .registers 6
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/List",
            "<",
            "Ljava/lang/String;",
            ">;)",
            "Ljava/lang/String;"
        }
    .end annotation

    .prologue
    .line 56
    .local p0, "dexes":Ljava/util/List;, "Ljava/util/List<Ljava/lang/String;>;"
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    .line 57
    .local v2, "pathBuilder":Ljava/lang/StringBuilder;
    const/4 v1, 0x1

    .line 58
    .local v1, "first":Z
    invoke-interface {p0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v3

    :goto_a
    invoke-interface {v3}, Ljava/util/Iterator;->hasNext()Z

    move-result v4

    if-eqz v4, :cond_23

    invoke-interface {v3}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/lang/String;

    .line 59
    .local v0, "dex":Ljava/lang/String;
    if-eqz v1, :cond_1d

    .line 60
    const/4 v1, 0x0

    .line 64
    :goto_19
    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    goto :goto_a

    .line 62
    :cond_1d
    sget-object v4, Ljava/io/File;->pathSeparator:Ljava/lang/String;

    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    goto :goto_19

    .line 66
    .end local v0    # "dex":Ljava/lang/String;
    :cond_23
    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    return-object v3
.end method

.method public static inject(Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)Lcom/wanpg/yauld/loader/YauldDexClassLoader;
    .registers 5
    .param p0, "classLoader"    # Ljava/lang/ClassLoader;
    .param p1, "nativeLibraryPath"    # Ljava/lang/String;
    .param p2, "codeCacheDir"    # Ljava/lang/String;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/ClassLoader;",
            "Ljava/lang/String;",
            "Ljava/lang/String;",
            "Ljava/util/List",
            "<",
            "Ljava/lang/String;",
            ">;)",
            "Lcom/wanpg/yauld/loader/YauldDexClassLoader;"
        }
    .end annotation

    .prologue
    .line 84
    .local p3, "dexes":Ljava/util/List;, "Ljava/util/List<Ljava/lang/String;>;"
    new-instance v0, Lcom/wanpg/yauld/loader/YauldDexClassLoader;

    invoke-direct {v0, p0, p1, p2, p3}, Lcom/wanpg/yauld/loader/YauldDexClassLoader;-><init>(Ljava/lang/ClassLoader;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V

    .line 85
    .local v0, "yauldDexClassLoader":Lcom/wanpg/yauld/loader/YauldDexClassLoader;
    invoke-static {p0, v0}, Lcom/wanpg/yauld/loader/YauldDexClassLoader;->setParent(Ljava/lang/ClassLoader;Ljava/lang/ClassLoader;)V

    .line 86
    return-object v0
.end method

.method private onLoadComplete()V
    .registers 2

    .prologue
    .line 26
    const/4 v0, 0x1

    iput-boolean v0, p0, Lcom/wanpg/yauld/loader/YauldDexClassLoader;->isLoadFinish:Z

    .line 27
    return-void
.end method

.method private static setParent(Ljava/lang/ClassLoader;Ljava/lang/ClassLoader;)V
    .registers 6
    .param p0, "classLoader"    # Ljava/lang/ClassLoader;
    .param p1, "newParent"    # Ljava/lang/ClassLoader;

    .prologue
    .line 71
    :try_start_0
    const-class v2, Ljava/lang/ClassLoader;

    const-string v3, "parent"

    invoke-virtual {v2, v3}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v1

    .line 72
    .local v1, "parent":Ljava/lang/reflect/Field;
    const/4 v2, 0x1

    invoke-virtual {v1, v2}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 73
    invoke-virtual {v1, p0, p1}, Ljava/lang/reflect/Field;->set(Ljava/lang/Object;Ljava/lang/Object;)V
    :try_end_f
    .catch Ljava/lang/IllegalArgumentException; {:try_start_0 .. :try_end_f} :catch_10
    .catch Ljava/lang/IllegalAccessException; {:try_start_0 .. :try_end_f} :catch_17
    .catch Ljava/lang/NoSuchFieldException; {:try_start_0 .. :try_end_f} :catch_1e

    .line 81
    return-void

    .line 74
    .end local v1    # "parent":Ljava/lang/reflect/Field;
    :catch_10
    move-exception v0

    .line 75
    .local v0, "e":Ljava/lang/IllegalArgumentException;
    new-instance v2, Ljava/lang/RuntimeException;

    invoke-direct {v2, v0}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/Throwable;)V

    throw v2

    .line 76
    .end local v0    # "e":Ljava/lang/IllegalArgumentException;
    :catch_17
    move-exception v0

    .line 77
    .local v0, "e":Ljava/lang/IllegalAccessException;
    new-instance v2, Ljava/lang/RuntimeException;

    invoke-direct {v2, v0}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/Throwable;)V

    throw v2

    .line 78
    .end local v0    # "e":Ljava/lang/IllegalAccessException;
    :catch_1e
    move-exception v0

    .line 79
    .local v0, "e":Ljava/lang/NoSuchFieldException;
    new-instance v2, Ljava/lang/RuntimeException;

    invoke-direct {v2, v0}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/Throwable;)V

    throw v2
.end method


# virtual methods
.method public findClass(Ljava/lang/String;)Ljava/lang/Class;
    .registers 4
    .param p1, "className"    # Ljava/lang/String;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/lang/String;",
            ")",
            "Ljava/lang/Class",
            "<*>;"
        }
    .end annotation

    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/ClassNotFoundException;
        }
    .end annotation

    .prologue
    .line 31
    :try_start_0
    iget-object v1, p0, Lcom/wanpg/yauld/loader/YauldDexClassLoader;->delegateClassLoader:Lcom/wanpg/yauld/loader/YauldDexClassLoader$DelegateClassLoader;

    invoke-virtual {v1, p1}, Lcom/wanpg/yauld/loader/YauldDexClassLoader$DelegateClassLoader;->findClass(Ljava/lang/String;)Ljava/lang/Class;
    :try_end_5
    .catch Ljava/lang/ClassNotFoundException; {:try_start_0 .. :try_end_5} :catch_7

    move-result-object v1

    return-object v1

    .line 32
    :catch_7
    move-exception v0

    .line 33
    .local v0, "e":Ljava/lang/ClassNotFoundException;
    throw v0
.end method

.method public isLoadFinish()Z
    .registers 2

    .prologue
    .line 38
    iget-boolean v0, p0, Lcom/wanpg/yauld/loader/YauldDexClassLoader;->isLoadFinish:Z

    return v0
.end method
