<template id="edge-overview">
    <div>
        <ul class="edge-overview-list">
            <li>a</li>
            <li>b</li>
        </ul>
    </div>
</template>
<script>
    Vue.component("edge-overview", {
        template: "#dge-overview",
        data: () => ({
            edges: [],
        }),
        created() {
           this.edges = ["a",'b']
        }
    });
</script>
<style>
    ul.edge-overview-list {
        padding: 0;
        list-style: none;
    }
    ul.edge-overview-list a {
        display: block;
        padding: 16px;
        border-bottom: 1px solid #ddd;
    }
    ul.edge-overview-list a:hover {
        background: #00000010;
    }
</style>