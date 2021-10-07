<template id="richnlg">


    <div class="card text-center m-3">
        <h5 class="card-header">{{name}}</h5>
        <input style="width:500px"

               v-model="msg">
        <div class="card-body">{{msg}} </div>
    </div>
</template>

<script>
var p = Vue.component("richnlg", {
  name: "post-request",
  template: "#richnlg",
  data: () => ({
           msg:"Beth Ann has an umbrella",
           name: "Rich NLU"
        }),
  created() {

  },
  });
</script>
