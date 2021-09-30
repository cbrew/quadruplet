<template id="hello-world">
    <div>
    <h1 class="hello-world">Hello, World!</h1>
    <table>
      <tr>
        <th>Month</th>
        <th>Savings</th>
      </tr>
      <tr>
        <td>January</td>
        <td>$100</td>
      </tr>
      <tr>
        <td>February</td>
        <td>$80</td>
      </tr>
      <tr>
        <td colspan="2">Sum: $180</td>
      </tr>
    </table>
    </div>
</template>
<script>
    Vue.component("hello-world", {template: "#hello-world"});
</script>
<style>
    .hello-world {
        color: goldenrod;
    }
    table, th, td {
    border: 1px solid black;
    }
</style>