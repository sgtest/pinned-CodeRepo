<template>
    <div class="login-container">
        <div class="login-logo">
            <span>{{ getThemeConfig.globalViceTitle }}</span>
        </div>
        <div class="login-content" :class="{ 'login-content-mobile': tabsActiveName === 'mobile' }">
            <div class="login-content-main">
                <h4 class="login-content-title">mayfly-cloud</h4>
                <el-tabs v-model="tabsActiveName" @tab-click="onTabsClick">
                    <el-tab-pane label="账号密码登录" name="account" :disabled="tabsActiveName === 'account'">
                        <transition name="el-zoom-in-center">
                            <account-login v-show="isTabPaneShow" />
                        </transition>
                    </el-tab-pane>
                    <!-- <el-tab-pane label="手机号登录" name="mobile" :disabled="tabsActiveName === 'mobile'">
                        <transition name="el-zoom-in-center">
                            <Mobile v-show="!isTabPaneShow" />
                        </transition>
                    </el-tab-pane> -->
                </el-tabs>
                <!-- <div class="mt10">
                    <el-button type="text" size="small">第三方登录</el-button>
                    <el-button type="text" size="small">友情链接</el-button>
                </div> -->
            </div>
        </div>
        <div class="login-copyright">
            <div class="mb5 login-copyright-company">mayfly</div>
            <div class="login-copyright-msg">mayfly</div>
        </div>
    </div>
</template>

<script lang="ts">
import { toRefs, reactive, computed } from 'vue';
import AccountLogin from '@/views/login/component/AccountLogin.vue';
// import Mobile from '@/views/login/component/mobile.vue';
import { useStore } from '@/store/index.ts';
export default {
    name: 'LoginPage',
    components: { AccountLogin },
    setup() {
        const store = useStore();
        const state = reactive({
            tabsActiveName: 'account',
            isTabPaneShow: true,
        });
        // 获取布局配置信息
        const getThemeConfig = computed(() => {
            return store.state.themeConfig.themeConfig;
        });
        // 切换密码、手机登录
        const onTabsClick = () => {
            state.isTabPaneShow = !state.isTabPaneShow;
        };
        return {
            onTabsClick,
            getThemeConfig,
            ...toRefs(state),
        };
    },
};
</script>

<style scoped lang="scss">
.login-container {
    width: 100%;
    height: 100%;
    background: url('@/assets/image/bg-login.png') no-repeat;
    background-size: 100% 100%;
    .login-logo {
        position: absolute;
        top: 30px;
        left: 50%;
        height: 50px;
        display: flex;
        align-items: center;
        font-size: 20px;
        color: var(--color-primary);
        letter-spacing: 2px;
        width: 90%;
        transform: translateX(-50%);
    }
    .login-content {
        width: 500px;
        padding: 20px;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%) translate3d(0, 0, 0);
        background-color: rgba(255, 255, 255, 0.99);
        box-shadow: 0 2px 12px 0 var(--color-primary-light-5);
        border-radius: 4px;
        transition: height 0.2s linear;
        height: 480px;
        overflow: hidden;
        z-index: 1;
        .login-content-main {
            margin: 0 auto;
            width: 80%;
            .login-content-title {
                color: #333;
                font-weight: 500;
                font-size: 22px;
                text-align: center;
                letter-spacing: 4px;
                margin: 15px 0 30px;
                white-space: nowrap;
            }
        }
    }
    .login-content-mobile {
        height: 418px;
    }
    .login-copyright {
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
        bottom: 30px;
        text-align: center;
        color: white;
        font-size: 12px;
        opacity: 0.8;
        .login-copyright-company {
            white-space: nowrap;
        }
        .login-copyright-msg {
            @extend .login-copyright-company;
        }
    }
}
</style>
