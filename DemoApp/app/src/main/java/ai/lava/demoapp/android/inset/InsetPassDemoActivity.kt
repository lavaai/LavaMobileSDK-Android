package ai.lava.demoapp.android.inset

import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lava.lavasdk.Lava
import com.lava.lavasdk.PassNavigationResult
import com.lava.lavasdk.PassPage
import com.lava.lavasdk.PassPresentation

private enum class ClubTab(val title: String) {
  HOME("Home"),
  MATCHES("Matches"),
  PLAYERS("Players"),
  PASS("Pass")
}

@OptIn(ExperimentalMaterial3Api::class)
class InsetPassDemoActivity : AppCompatActivity() {

  private var selectedTab by mutableStateOf(ClubTab.HOME)
  private var passVisible = false
  private var hidingPass = false
  private var suppressFinishUntil = 0L
  private var topInsetDp = TOP_BAR_HEIGHT_DP
  private var bottomInsetDp = TAB_BAR_HEIGHT_DP

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    Lava.instance.setPassLifecycleListener { _ ->
      onPassHidden()
    }

    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          if (hidingPass || SystemClock.uptimeMillis() < suppressFinishUntil) {
            return
          }
          if (passVisible) {
            requestLeavePass { selectedTab = ClubTab.HOME }
          } else {
            finish()
          }
        }
      }
    )

    setContent {
      MaterialTheme {
        InsetPassDemoScreen(
          selectedTab = selectedTab,
          onSelectTab = { selectTab(it) },
          onChromeInsets = { topDp, bottomDp ->
            topInsetDp = topDp
            bottomInsetDp = bottomDp
          }
        )
      }
    }
  }

  private fun selectTab(tab: ClubTab) {
    if (tab == selectedTab) {
      if (tab == ClubTab.PASS && !passVisible) {
        showPass()
      }
      return
    }

    if (tab == ClubTab.PASS) {
      selectedTab = tab
      showPass()
      return
    }

    if (passVisible) {
      requestLeavePass {
        selectedTab = tab
      }
    } else {
      selectedTab = tab
    }
  }

  private fun showPass() {
    Lava.instance.showInAppPass(
      this,
      PassPresentation.inset(top = topInsetDp, bottom = bottomInsetDp),
      PassPage.PASS
    )
    passVisible = true
  }

  private fun requestLeavePass(onAllowed: () -> Unit) {
    if (hidingPass) {
      return
    }
    hidingPass = true
    Lava.instance.requestHideInAppPass { result ->
      hidingPass = false
      when (result) {
        is PassNavigationResult.Allowed -> {
          onPassHidden()
          onAllowed()
        }
        is PassNavigationResult.Blocked -> {
          selectedTab = ClubTab.PASS
          Toast.makeText(
            this,
            result.reason ?: "Stay on the pass to finish the form",
            Toast.LENGTH_SHORT
          ).show()
        }
      }
    }
  }

  private fun onPassHidden() {
    passVisible = false
    hidingPass = false
    suppressFinishUntil = SystemClock.uptimeMillis() + FINISH_SUPPRESS_MS
    if (selectedTab == ClubTab.PASS) {
      selectedTab = ClubTab.HOME
    }
  }

  override fun onDestroy() {
    Lava.instance.setPassLifecycleListener(null)
    if (isFinishing) {
      Lava.instance.hideInAppPass(true)
    }
    super.onDestroy()
  }
}

private val ClubNavy = Color(0xFF0B1F3A)
private val ClubGold = Color(0xFFE8B923)
private val ClubIce = Color(0xFFF4F6F8)
private const val TOP_BAR_HEIGHT_DP = 64
private const val TAB_BAR_HEIGHT_DP = 80
private const val FINISH_SUPPRESS_MS = 750L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InsetPassDemoScreen(
  selectedTab: ClubTab,
  onSelectTab: (ClubTab) -> Unit,
  onChromeInsets: (topDp: Int, bottomDp: Int) -> Unit
) {
  val density = LocalDensity.current
  var topBarHeightPx by remember { mutableStateOf(0) }
  var bottomBarHeightPx by remember { mutableStateOf(0) }
  LaunchedEffect(topBarHeightPx, bottomBarHeightPx) {
    if (topBarHeightPx == 0 || bottomBarHeightPx == 0) {
      return@LaunchedEffect
    }
    onChromeInsets(
      (topBarHeightPx / density.density).toInt(),
      (bottomBarHeightPx / density.density).toInt()
    )
  }

  Scaffold(
    containerColor = ClubIce,
    topBar = {
      CenterAlignedTopAppBar(
        modifier = Modifier.onGloballyPositioned { topBarHeightPx = it.size.height },
        title = {
          Text("DUCKS", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = ClubNavy,
          titleContentColor = ClubGold
        )
      )
    },
    bottomBar = {
      NavigationBar(
        modifier = Modifier.onGloballyPositioned { bottomBarHeightPx = it.size.height },
        containerColor = ClubNavy,
        contentColor = Color.White
      ) {
        NavigationBarItem(
          selected = selectedTab == ClubTab.HOME,
          onClick = { onSelectTab(ClubTab.HOME) },
          icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
          label = { Text("Home") }
        )
        NavigationBarItem(
          selected = selectedTab == ClubTab.MATCHES,
          onClick = { onSelectTab(ClubTab.MATCHES) },
          icon = { Icon(Icons.Filled.DateRange, contentDescription = "Matches") },
          label = { Text("Matches") }
        )
        NavigationBarItem(
          selected = selectedTab == ClubTab.PLAYERS,
          onClick = { onSelectTab(ClubTab.PLAYERS) },
          icon = { Icon(Icons.Filled.Groups, contentDescription = "Players") },
          label = { Text("Players") }
        )
        NavigationBarItem(
          selected = selectedTab == ClubTab.PASS,
          onClick = { onSelectTab(ClubTab.PASS) },
          icon = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = "Pass") },
          label = { Text("Pass") }
        )
      }
    }
  ) { innerPadding ->
    when (selectedTab) {
      ClubTab.HOME -> HomeTab(innerPadding)
      ClubTab.MATCHES -> MatchesTab(innerPadding)
      ClubTab.PLAYERS -> PlayersTab(innerPadding)
      ClubTab.PASS -> PassPlaceholder(innerPadding)
    }
  }
}

@Composable
private fun HomeTab(innerPadding: PaddingValues) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(innerPadding)
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text("Welcome back, fan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    ClubCard(title = "Next home game", body = "Ducks vs Kings\nSaturday 7:00 PM · Honda Center")
    ClubCard(title = "Season", body = "12–8–2 · 3rd in Pacific")
  }
}

@Composable
private fun MatchesTab(innerPadding: PaddingValues) {
  val matches = listOf(
    "Sat  ·  Ducks vs Kings  ·  Honda Center",
    "Tue  ·  Ducks @ Sharks  ·  SAP Center",
    "Fri  ·  Ducks vs Oilers  ·  Honda Center",
    "Sun  ·  Ducks @ Knights  ·  T-Mobile Arena"
  )
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(innerPadding),
    contentPadding = PaddingValues(20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text("Schedule", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
      Spacer(Modifier.height(8.dp))
    }
    items(matches) { match ->
      ClubCard(title = match.substringBefore("·").trim(), body = match.substringAfter("·").trim())
    }
  }
}

@Composable
private fun PlayersTab(innerPadding: PaddingValues) {
  val players = listOf(
    "C  ·  #11  ·  Trevor Zegras",
    "W  ·  #38  ·  Ryan Kesler",
    "D  ·  #4   ·  Cam Fowler",
    "G  ·  #36  ·  John Gibson"
  )
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(innerPadding),
    contentPadding = PaddingValues(20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text("Roster", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
      Spacer(Modifier.height(8.dp))
    }
    items(players) { player ->
      ClubCard(title = player.substringAfterLast("·").trim(), body = player.substringBeforeLast("·").trim())
    }
  }
}

@Composable
private fun PassPlaceholder(innerPadding: PaddingValues) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(innerPadding)
      .background(ClubIce),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text("Opening member pass…", color = Color.Gray)
  }
}

@Composable
private fun ClubCard(title: String, body: String) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Color.White)
  ) {
    Row(modifier = Modifier.padding(16.dp)) {
      Column {
        Text(title, fontWeight = FontWeight.Bold, color = ClubNavy)
        Spacer(Modifier.height(4.dp))
        Text(body, color = Color.DarkGray)
      }
    }
  }
}
