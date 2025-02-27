<template>
    <div class="role-list">
        <div class="toolbar">
            <el-button v-auth="'account:add'" type="primary" icon="plus" @click="editAccount(true)">添加</el-button>
            <el-button v-auth="'account:update'" :disabled="chooseId == null" @click="editAccount(false)" type="primary" icon="Edit">编辑</el-button>
            <el-button v-auth="'account:saveRoles'" :disabled="chooseId == null" @click="roleEdit()" type="success" icon="Setting"
                >角色分配</el-button
            >
            <el-button v-auth="'account:del'" :disabled="chooseId == null" @click="deleteAccount()" type="danger" icon="Delete">删除</el-button>
            <div style="float: right">
                <el-input
                    placeholder="请输入账号名"
                    style="width: 140px"
                    v-model="query.username"
                    @clear="search()"
                    clearable
                ></el-input>
                <el-button @click="search()" type="success" icon="search"></el-button>
            </div>
        </div>
        <el-table :data="datas" border ref="table" @current-change="choose" show-overflow-tooltip>
            <el-table-column label="选择" width="50px">
                <template #default="scope">
                    <el-radio v-model="chooseId" :label="scope.row.id">
                        <i></i>
                    </el-radio>
                </template>
            </el-table-column>
            <el-table-column prop="username" label="用户名" min-width="115"></el-table-column>

            <el-table-column align="center" prop="status" label="状态" min-width="63">
                <template #default="scope">
                    <el-tag v-if="scope.row.status == 1" type="success" size="small">正常</el-tag>
                    <el-tag v-if="scope.row.status == 0" type="danger" size="small">禁用</el-tag>
                </template>
            </el-table-column>

            <!-- <el-table-column min-width="115" prop="creator" label="创建账号"></el-table-column> -->
            <el-table-column min-width="160" prop="createTime" label="创建时间">
                <template #default="scope">
                    {{ $filters.dateFormat(scope.row.createTime) }}
                </template>
            </el-table-column>
            <!-- <el-table-column min-width="115" prop="modifier" label="更新账号"></el-table-column>
			<el-table-column min-width="160" prop="updateTime" label="修改时间">
				<template #default="scope">
					{{ $filters.dateFormat(scope.row.updateTime) }}
				</template>
			</el-table-column> -->
            <el-table-column min-width="160" prop="lastLoginTime" label="最后登录时间"></el-table-column>
            <el-table-column min-width="120" prop="remark" label="备注" show-overflow-tooltip></el-table-column>
            <el-table-column label="查看更多" min-width="150">
                <template #default="scope">
                    <el-link @click.prevent="showRoles(scope.row)" type="success">角色</el-link>

                    <el-link class="ml5" @click.prevent="showResources(scope.row)" type="info">菜单&权限</el-link>
                </template>
            </el-table-column>

            <el-table-column label="操作" min-width="200px">
                <template #default="scope">
                    <el-button
                        v-auth="'account:changeStatus'"
                        v-if="scope.row.status == 1"
                        type="danger"
                        @click="changeStatus(scope.row)"
                        icom="el-icon-tickets"
                        size="small"
                        plain
                        >禁用</el-button
                    >
                    <el-button
                        v-auth="'account:changeStatus'"
                        v-if="scope.row.status == 0"
                        type="success"
                        @click="changeStatus(scope.row)"
                        size="small"
                        plain
                        >启用</el-button
                    >
                </template>
            </el-table-column>
        </el-table>
        <el-pagination
            @current-change="handlePageChange"
            style="text-align: center"
            background
            layout="prev, pager, next, total, jumper"
            :total="total"
            v-model:current-page="query.pageNum"
            :page-size="query.pageSize"
        />

        <el-dialog width="500px" :title="showRoleDialog.title" v-model="showRoleDialog.visible">
            <el-table border :data="showRoleDialog.accountRoles">
                <el-table-column property="name" label="角色名" width="125"></el-table-column>
                <el-table-column property="creator" label="分配账号" width="125"></el-table-column>
                <el-table-column property="createTime" label="分配时间">
                    <template #default="scope">
                        {{ $filters.dateFormat(scope.row.createTime) }}
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>

        <el-dialog :title="showResourceDialog.title" v-model="showResourceDialog.visible" width="400px">
            <el-tree style="height: 50vh; overflow: auto" :data="showResourceDialog.resources" node-key="id" :props="showResourceDialog.defaultProps">
                <template #default="{ node, data }">
                    <span class="custom-tree-node">
                        <span v-if="data.type == enums.ResourceTypeEnum.MENU.value">{{ node.label }}</span>
                        <span v-if="data.type == enums.ResourceTypeEnum.PERMISSION.value" style="color: #67c23a">{{ node.label }}</span>
                    </span>
                </template>
            </el-tree>
        </el-dialog>

        <role-edit v-model:visible="roleDialog.visible" :account="roleDialog.account" @cancel="cancel()" />
        <account-edit v-model:visible="accountDialog.visible" v-model:account="accountDialog.data" @val-change="valChange()" />
    </div>
</template>

<script lang='ts'>
import { toRefs, reactive, onMounted, defineComponent } from 'vue';
import RoleEdit from './RoleEdit.vue';
import AccountEdit from './AccountEdit.vue';
import enums from '../enums';
import { accountApi } from '../api';
import { ElMessage } from 'element-plus';
export default defineComponent({
    name: 'AccountList',
    components: {
        RoleEdit,
        AccountEdit,
    },
    setup() {
        const state = reactive({
            chooseId: null,
            /**
             * 选中的数据
             */
            chooseData: null,
            /**
             * 查询条件
             */
            query: {
                pageNum: 1,
                pageSize: 10,
            },
            datas: [],
            total: 0,
            showRoleDialog: {
                title: '',
                visible: false,
                accountRoles: [],
            },
            showResourceDialog: {
                title: '',
                visible: false,
                resources: [],
                defaultProps: {
                    children: 'children',
                    label: 'name',
                },
            },
            roleDialog: {
                visible: false,
                account: null,
                roles: [],
            },
            accountDialog: {
                visible: false,
                data: null,
            },
        });

        onMounted(() => {
            search();
        });

        const choose = (item: any) => {
            if (!item) {
                return;
            }
            state.chooseId = item.id;
            state.chooseData = item;
        };

        const search = async () => {
            let res: any = await accountApi.list.request(state.query);
            state.datas = res.list;
            state.total = res.total;
        };

        const showResources = async (row: any) => {
            let showResourceDialog = state.showResourceDialog;
            showResourceDialog.title = '"' + row.username + '" 的菜单&权限';
            showResourceDialog.resources = [];
            showResourceDialog.resources = await accountApi.resources.request({
                id: row.id,
            });
            showResourceDialog.visible = true;
        };

        const showRoles = async (row: any) => {
            let showRoleDialog = state.showRoleDialog;
            showRoleDialog.title = '"' + row.username + '" 的角色信息';
            showRoleDialog.accountRoles = await accountApi.roles.request({
                id: row.id,
            });
            showRoleDialog.visible = true;
        };

        const changeStatus = async (row: any) => {
            let id = row.id;
            let status = row.status ? 0 : 1;
            await accountApi.changeStatus.request({
                id,
                status,
            });
            ElMessage.success('操作成功');
            search();
        };

        const handlePageChange = (curPage: number) => {
            state.query.pageNum = curPage;
            search();
        };

        const roleEdit = () => {
            if (!state.chooseId) {
                ElMessage.error('请选择账号');
            }
            state.roleDialog.visible = true;
            state.roleDialog.account = state.chooseData;
        };

        const editAccount = (isAdd = false) => {
            if (isAdd) {
                state.accountDialog.data = null;
            } else {
                state.accountDialog.data = state.chooseData;
            }
            state.accountDialog.visible = true;
        };

        const cancel = () => {
            state.roleDialog.visible = false;
            state.roleDialog.account = null;
            search();
        };

        const valChange = () => {
            state.accountDialog.visible = false;
            search();
        };

        const deleteAccount = async () => {
            try {
                await accountApi.del.request({ id: state.chooseId });
                ElMessage.success('删除成功');
                search();
            } catch (error) {
                ElMessage.error('刪除失败');
            }
        };

        return {
            ...toRefs(state),
            enums,
            search,
            choose,
            showResources,
            showRoles,
            changeStatus,
            handlePageChange,
            roleEdit,
            editAccount,
            cancel,
            valChange,
            deleteAccount,
        };
    },
});
</script>
<style lang="scss">
</style>
