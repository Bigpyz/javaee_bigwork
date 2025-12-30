<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vue.js 登录页面</title>
    <!-- 引入 Vue 3 -->
    <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
    <!-- 引入 Element Plus 样式 -->
    <link rel="stylesheet" href="https://unpkg.com/element-plus/dist/index.css">
    <!-- 引入 Element Plus 组件库 -->
    <script src="https://unpkg.com/element-plus/dist/index.full.js"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Microsoft YaHei', sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }

        .login-container {
            width: 100%;
            max-width: 420px;
        }

        .login-card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 16px;
            padding: 40px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }

        .login-card:hover {
            transform: translateY(-5px);
        }

        .login-header {
            text-align: center;
            margin-bottom: 30px;
        }

        .login-header h1 {
            font-size: 28px;
            color: #2d3748;
            margin-bottom: 8px;
            font-weight: 700;
        }

        .login-header p {
            color: #718096;
            font-size: 14px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-label {
            display: block;
            margin-bottom: 6px;
            color: #4a5568;
            font-size: 14px;
            font-weight: 500;
        }

        .form-input {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e2e8f0;
            border-radius: 10px;
            font-size: 15px;
            transition: all 0.3s;
            background: #f7fafc;
        }

        .form-input:focus {
            outline: none;
            border-color: #667eea;
            background: white;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .btn-submit {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
            margin-top: 10px;
        }

        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
        }

        .btn-submit:active {
            transform: translateY(0);
        }

        .error-message {
            background: #fed7d7;
            color: #c53030;
            padding: 12px 16px;
            border-radius: 10px;
            margin-bottom: 20px;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .form-footer {
            margin-top: 25px;
            text-align: center;
        }

        .remember-forgot {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .remember-me {
            display: flex;
            align-items: center;
            gap: 8px;
            color: #4a5568;
            font-size: 14px;
        }

        .forgot-password {
            color: #667eea;
            text-decoration: none;
            font-size: 14px;
        }

        .forgot-password:hover {
            text-decoration: underline;
        }

        .test-account {
            margin-top: 25px;
            padding: 15px;
            background: #f7fafc;
            border-radius: 10px;
            text-align: center;
        }

        .test-account h3 {
            color: #4a5568;
            font-size: 13px;
            margin-bottom: 5px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .test-account p {
            color: #718096;
            font-size: 12px;
            line-height: 1.5;
        }

        .loading-overlay {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(255, 255, 255, 0.9);
            display: flex;
            justify-content: center;
            align-items: center;
            border-radius: 16px;
            z-index: 10;
        }

        .loading-spinner {
            width: 40px;
            height: 40px;
            border: 3px solid #e2e8f0;
            border-top-color: #667eea;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .success-message {
            background: #c6f6d5;
            color: #22543d;
            padding: 12px 16px;
            border-radius: 10px;
            margin-bottom: 20px;
            text-align: center;
        }

        /* 响应式设计 */
        @media (max-width: 480px) {
            .login-card {
                padding: 30px 20px;
            }

            .login-header h1 {
                font-size: 24px;
            }

            .remember-forgot {
                flex-direction: column;
                align-items: flex-start;
                gap: 10px;
            }
        }
    </style>
</head>
<body>
    <div id="app">
        <div class="login-container">
            <div class="login-card">
                <!-- 登录头部 -->
                <div class="login-header">
                    <h1>用户登录</h1>
                    <p>欢迎回来，请登录您的账户</p>
                </div>

                <!-- 错误消息 -->
                <div v-if="errorMessage" class="error-message">
                    <span>⚠️</span>
                    <span>{{ errorMessage }}</span>
                </div>

                <!-- 成功消息 -->
                <div v-if="successMessage" class="success-message">
                    ✅ {{ successMessage }}
                </div>

                <!-- 登录表单 -->
                <form @submit.prevent="handleLogin">
                    <div class="form-group">
                        <label for="username" class="form-label">用户名</label>
                        <input
                            type="text"
                            id="username"
                            v-model="form.username"
                            class="form-input"
                            placeholder="请输入用户名"
                            required
                            autocomplete="username"
                            autofocus
                        >
                    </div>

                    <div class="form-group">
                        <label for="password" class="form-label">密码</label>
                        <input
                            :type="showPassword ? 'text' : 'password'"
                            id="password"
                            v-model="form.password"
                            class="form-input"
                            placeholder="请输入密码"
                            required
                            autocomplete="current-password"
                        >
                        <div style="margin-top: 8px;">
                            <input
                                type="checkbox"
                                id="showPassword"
                                v-model="showPassword"
                                style="margin-right: 5px;"
                            >
                            <label for="showPassword" style="color: #4a5568; font-size: 13px;">
                                显示密码
                            </label>
                        </div>
                    </div>

                    <div class="remember-forgot">
                        <div class="remember-me">
                            <input
                                type="checkbox"
                                id="rememberMe"
                                v-model="form.rememberMe"
                            >
                            <label for="rememberMe">记住我</label>
                        </div>
                        <a href="#" class="forgot-password" @click.prevent="showForgetPassword = true">
                            忘记密码？
                        </a>
                    </div>

                    <button
                        type="submit"
                        class="btn-submit"
                        :disabled="loading"
                    >
                        <span v-if="!loading">登录</span>
                        <span v-else>登录中...</span>
                    </button>
                </form>

                <!-- 测试账户 -->
                <div class="test-account">
                    <h3>测试账户</h3>
                    <p>用户名: admin | 密码: 123456</p>
                    <p>用户名: user1 | 密码: password1</p>
                </div>

                <!-- 加载遮罩 -->
                <div v-if="loading" class="loading-overlay">
                    <div class="loading-spinner"></div>
                </div>

                <!-- 忘记密码弹窗 -->
                <div v-if="showForgetPassword" class="loading-overlay">
                    <div style="background: white; padding: 30px; border-radius: 10px; text-align: center;">
                        <h3 style="margin-bottom: 15px; color: #2d3748;">重置密码</h3>
                        <p style="color: #718096; margin-bottom: 20px;">
                            请联系系统管理员重置密码<br>
                            或发送邮件至 admin@example.com
                        </p>
                        <button
                            @click="showForgetPassword = false"
                            style="padding: 8px 20px; background: #667eea; color: white; border: none; border-radius: 6px; cursor: pointer;"
                        >
                            关闭
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        const { createApp, ref, reactive } = Vue;

        createApp({
            setup() {
                // 表单数据
                const form = reactive({
                    username: '',
                    password: '',
                    rememberMe: false
                });

                // 状态
                const showPassword = ref(false);
                const loading = ref(false);
                const errorMessage = ref('');
                const successMessage = ref('');
                const showForgetPassword = ref(false);

                // 模拟用户数据
                const users = [
                    { username: 'admin', password: '123456' },
                    { username: 'user1', password: 'password1' }
                ];

                // 模拟API请求
                const mockApiLogin = (username, password) => {
                    return new Promise((resolve, reject) => {
                        setTimeout(() => {
                            const user = users.find(u =>
                                u.username === username && u.password === password
                            );

                            if (user) {
                                resolve({
                                    success: true,
                                    data: {
                                        id: 1,
                                        username: user.username,
                                        token: 'fake-jwt-token-' + Date.now()
                                    }
                                });
                            } else {
                                reject({
                                    success: false,
                                    message: '用户名或密码错误'
                                });
                            }
                        }, 1500);
                    });
                };

                // 处理登录
                const handleLogin = async () => {
                    // 重置消息
                    errorMessage.value = '';
                    successMessage.value = '';

                    // 验证输入
                    if (!form.username.trim() || !form.password.trim()) {
                        errorMessage.value = '请输入用户名和密码';
                        return;
                    }

                    // 开始加载
                    loading.value = true;

                    try {
                        const response = await mockApiLogin(form.username, form.password);

                        if (response.success) {
                            successMessage.value = '登录成功！正在跳转...';

                            // 在实际应用中，这里会保存 token
                            if (form.rememberMe) {
                                localStorage.setItem('userToken', response.data.token);
                                localStorage.setItem('username', response.data.username);
                            } else {
                                sessionStorage.setItem('userToken', response.data.token);
                            }

                            // 模拟跳转延迟
                            setTimeout(() => {
                                alert(`登录成功！欢迎 ${response.data.username}`);
                                // 在实际应用中，这里会跳转到主页
                                // window.location.href = '/dashboard';
                            }, 1000);
                        }
                    } catch (error) {
                        errorMessage.value = error.message || '登录失败，请重试';
                    } finally {
                        loading.value = false;
                    }
                };

                // 页面加载时检查记住的用户名
                const checkRememberedUser = () => {
                    const rememberedUser = localStorage.getItem('username');
                    if (rememberedUser) {
                        form.username = rememberedUser;
                        form.rememberMe = true;
                    }
                };

                //
                checkRememberedUser();

                return {
                    form,
                    showPassword,
                    loading,
                    errorMessage,
                    successMessage,
                    showForgetPassword,
                    handleLogin
                };
            }
        }).mount('#app');
    </script>
</body>
</html>