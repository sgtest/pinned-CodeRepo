<template>
    <div class="role-dialog">
        <el-dialog :title="title" v-model="dialogVisible" :show-close="false" :before-close="cancel" width="500px" :destroy-on-close="true">
            <el-form ref="roleForm" :model="form" label-width="90px">
                <el-form-item prop="name" label="角色名称:" required>
                    <el-input v-model="form.name" auto-complete="off"></el-input>
                </el-form-item>
                <el-form-item prop="code" label="角色标识:" required>
                    <el-input v-model="form.code" auto-complete="off"></el-input>
                </el-form-item>
                <el-form-item label="角色描述:">
                    <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入角色描述"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="cancel()">取 消</el-button>
                    <el-button type="primary" :loading="btnLoading" @click="btnOk">确 定</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts">
import { ref, toRefs, reactive, watch, defineComponent } from 'vue';
import { roleApi } from '../api';

export default defineComponent({
    name: 'RoleEdit',
    props: {
        visible: {
            type: Boolean,
        },
        data: {
            type: [Boolean, Object],
        },
        title: {
            type: String,
        },
    },
    setup(props: any, { emit }) {
        const state = reactive({
            dialogVisible: false,
            form: {
                id: null,
                name: '',
                code: '',
                status: 1,
                remark: '',
            },
            btnLoading: false,
        });

        const roleForm: any = ref(null);

        watch(props, (newValue) => {
            state.dialogVisible = newValue.visible;
            if (newValue.data) {
                state.form = { ...newValue.data };
            } else {
                state.form = {} as any;
            }
        });

        const cancel = () => {
            // 更新父组件visible prop对应的值为false
            emit('update:visible', false);
            emit('cancel');
        };

        const btnOk = async () => {
            let p = state.form.id ? roleApi.update : roleApi.save;
            await p.request(state.form);
            emit('val-change', state.form);
            cancel();
            state.btnLoading = true;
            setTimeout(() => {
                state.btnLoading = false;
            }, 1000);
        };

        return {
            ...toRefs(state),
            roleForm,
            btnOk,
            cancel,
        };
    },
});
</script>
<style lang="scss">
</style>
